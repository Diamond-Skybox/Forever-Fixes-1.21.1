#version 150

// Forever Fixes: a copy of Better Simple Clouds 1.2.4's rain_haze.fsh (MIT, falling-colud/better-simple-clouds) with one change,
// marked: the wet ground's face normal. It came from dFdx/dFdy of the view position, which the GPU takes over 2x2 pixel quads; a
// quad on the seam between two blocks mixed both and tilted the normal, so a dashed line of dry pixels ran along every block edge.
// The normal is rebuilt from the depth of the neighbouring pixels instead, on each axis from the side on the same surface.
// Also marked: only faces pointing up are wet (the original took faces pointing down as well).

// Better Simple Clouds: the rain on the world, over the whole frame at once (see RainHaze).
//
// Wet ground: a pixel of the game's terrain that lies on a flat top (the face normal, from the depth buffer's slope,
// points up) and is open to the sky (its height is the heightmap's for its column and the columns around it, blended
// across them, so the wet thins out over about a block under an overhang - blown-in rain, spreading water - instead
// of stopping along the block's edge) is darkened and cooled and mirrors the sky at grazing angles, as wet stone and
// soil do; where a world-space noise dips, water has collected into a puddle. The puddle is seen through, not painted
// over: the ground under it darker still, the sky mirrored a few percent from above and most of the way at a grazing
// angle (water's fresnel), and the rain pocking it with spreading rings. Then the haze: every pixel with depth - the
// game's terrain from the main depth
// buffer, and where that holds nothing, Distant Horizons' LODs from DH's own depth texture (DH draws them with its own
// projection and depth convention and writes no depth into the game's buffer) - is blended toward the fog colour by its
// distance from the eye. The sky, which has neither, is left as it is: its colour is the rain's already. The distances
// come from the projections inverted whole, so the view bob in the game's does no harm.

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D DhDepthSampler;
uniform sampler2D HeightmapSampler;
uniform mat4 InverseProjMat;
uniform mat4 InverseModelViewMat;
uniform mat4 DhInverseProjMat;
uniform float DhEnabled;
uniform float DhReverse; // 1 = DH writes reversed Z (its depth clears to 0, not 1)
uniform float DhRange;   // 1 = DH's clip Z runs 0..1 (else -1..1)
uniform float FogStart;
uniform float FogEnd;
uniform vec3 FogColor;
uniform float Strength;
uniform float Wetness;
uniform float WetRange;
uniform vec3 CameraPos;
uniform vec3 UpView;     // the world's up, in view space
uniform vec3 WetSkyColor;
uniform vec2 HeightmapOrigin;
uniform float HeightmapSize;
uniform float RainTime;  // seconds of game time (the post chain's own Time is a sawtooth of its own making)
uniform float RainLevel; // how much rain is falling, 0..1
uniform float CloudBase; // the cloud base above the camera, in blocks: how deep the rain is overhead
// Mode 1, run right after the near cloud pass with the camera in a cloud (RainHaze.coverHoles): paints over the gaps
// in the cloud around the camera. Past the solid sphere near the camera the mesh only holds faces that faced the
// camera when their chunk was meshed, so a line of sight that leaves the cloud through its far side finds no face at
// all and ends at the sky or at Distant Horizons' LODs: a sky-blue gap in the grey. A pixel with neither ground nor
// near cloud that is ringed by near cloud takes the ring's colour - the fogged cloud itself; open sky beside a cloud
// is not ringed and stays.
uniform sampler2D NearDepthSampler; // the near pass' depth: the ground's, and nearer where it drew cloud
uniform float Mode;
uniform float CoverRadius;          // the ring's radius as a share of the screen height
uniform vec2 OutSize;
// A translucent block holds its own distance in the depth buffer, and what is seen through it would get the haze of that
// distance: a window a few blocks away was a clear hole in the rain. So what lies behind the block gets the haze of its
// own distance, with the block taken off and put back (CloudGlass; GlassOn = 0 where nothing was captured this frame).
uniform sampler2D OpaqueDepthSampler; // the depth from before the translucent pass
uniform sampler2D GlassFilterSampler; // what a translucent block adds (rgb) and lets through (alpha)
uniform float GlassOn;

in vec2 texCoord;
out vec4 fragColor;

// === Forever Fixes: the face normal from the neighbouring pixels' depth, each axis taken from the side whose depth is closer to
// this pixel's (the same surface), so a block edge next to this pixel does not tilt it ===
vec3 foreverfixesViewAt(ivec2 pixel, ivec2 size)
{
    pixel = clamp(pixel, ivec2(0), size - 1);
    float depth = texelFetch(DiffuseDepthSampler, pixel, 0).x;
    vec4 view = InverseProjMat * vec4((vec2(pixel) + 0.5) / vec2(size) * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return view.xyz / view.w;
}

vec3 foreverfixesFaceNormal(vec3 centre)
{
    ivec2 size = textureSize(DiffuseDepthSampler, 0);
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    vec3 left = foreverfixesViewAt(pixel - ivec2(1, 0), size);
    vec3 right = foreverfixesViewAt(pixel + ivec2(1, 0), size);
    vec3 down = foreverfixesViewAt(pixel - ivec2(0, 1), size);
    vec3 up = foreverfixesViewAt(pixel + ivec2(0, 1), size);
    vec3 dx = abs(right.z - centre.z) < abs(centre.z - left.z) ? right - centre : centre - left;
    vec3 dy = abs(up.z - centre.z) < abs(centre.z - down.z) ? up - centre : centre - down;
    return normalize(cross(dx, dy));
}

// a hash and a value noise that stay sound at world coordinates (the inputs are wrapped first)
float hash12(vec2 p)
{
    p = mod(p, 1024.0);
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise(vec2 p)
{
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i), hash12(i + vec2(1.0, 0.0)), f.x), mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0, 1.0)), f.x), f.y);
}

// 1 where the column at heightmap texel t is open to the sky down to height y, else 0
float openAt(ivec2 t, float y)
{
    ivec2 size = textureSize(HeightmapSampler, 0);
    t = clamp(t, ivec2(0), size - 1);
    return y >= texelFetch(HeightmapSampler, t, 0).r - 0.08 ? 1.0 : 0.0;
}

// a column's openness softened by its four neighbours: an open column beside an overhang is four fifths open
float softOpenAt(ivec2 t, float y)
{
    return 0.6 * openAt(t, y) + 0.1 * (openAt(t + ivec2(1, 0), y) + openAt(t - ivec2(1, 0), y) + openAt(t + ivec2(0, 1), y) + openAt(t - ivec2(0, 1), y));
}

// how open to the sky the ground at world x, z is at height y, blended between the four nearest columns: a ramp
// over a block where an overhang begins, not a step at its edge
float exposure(vec2 worldXZ, float y)
{
    vec2 p = worldXZ - HeightmapOrigin - 0.5;   // texel centres on whole numbers
    vec2 i = floor(p);
    vec2 f = fract(p);
    ivec2 t = ivec2(i);
    float o00 = softOpenAt(t, y);
    float o10 = softOpenAt(t + ivec2(1, 0), y);
    float o01 = softOpenAt(t + ivec2(0, 1), y);
    float o11 = softOpenAt(t + ivec2(1, 1), y);
    return mix(mix(o00, o10, f.x), mix(o01, o11, f.x), f.y);
}

// the rain pocking a puddle: in every half-block cell a ring spreads from where the last drop landed and fades as it
// widens, then the next drop lands; the lighter the rain, the fewer cells see a drop in a given second. 0 = still
// water, 1 = on a ring
float ripples(vec2 worldXZ)
{
    vec2 p = worldXZ * 2.0;
    vec2 cell = floor(p);
    vec2 local = fract(p);
    float life = RainTime * 0.9 + hash12(cell);
    float t = fract(life);
    if (hash12(cell + floor(life) * 0.37) > mix(0.25, 1.0, RainLevel))
        return 0.0;
    vec2 centre = vec2(0.3) + 0.4 * vec2(hash12(cell + 7.1), hash12(cell + 3.7));
    float radius = 0.06 + 0.34 * t;
    float ring = 1.0 - smoothstep(0.0, 0.07, abs(length(local - centre) - radius));
    return ring * (1.0 - t) * (1.0 - t);
}

// The distance to Distant Horizons' LOD under this pixel; negative where there is none.
float lodDistance()
{
    if (DhEnabled <= 0.5)
        return -1.0;
    float raw = texture(DhDepthSampler, texCoord).x;
    if (DhReverse > 0.5 ? raw <= 0.0 : raw >= 1.0)
        return -1.0;
    float ndcZ = DhRange > 0.5 ? raw : raw * 2.0 - 1.0;
    vec4 lod = DhInverseProjMat * vec4(texCoord * 2.0 - 1.0, ndcZ, 1.0);
    return length(lod.xyz / lod.w);
}

// The air thickens with distance the way real air does, two thirds of the colour at the visibility distance, nearly all
// of it at twice that, never a wall.
float groundHaze(float dist)
{
    return (1.0 - exp(-pow(max(dist - FogStart, 0.0) / FogEnd, 1.3))) * Strength;
}

// The sky and the clouds: the rain is all around the camera, so the line of sight runs through it up to the cloud base - a
// long way at the horizon, where the air is as thick as over the farthest ground (the fog meets the ground's without a
// seam), a short way straight up, where the cloud deck shows through it. The deck is kept in view even in a cloudburst:
// at best half veiled overhead.
float skyHaze(vec3 viewPos)
{
    vec3 dir = normalize((InverseModelViewMat * vec4(viewPos, 0.0)).xyz);
    float elev = dir.y;
    float path = elev > 0.002 ? CloudBase / elev : 1.0e6;
    return (1.0 - exp(-pow(max(path - FogStart, 0.0) / FogEnd, 1.3))) * Strength * mix(1.0, 0.5, smoothstep(0.08, 0.6, elev));
}

void main()
{
    vec3 col = texture(DiffuseSampler, texCoord).rgb;
    float depth = texture(DiffuseDepthSampler, texCoord).x;
    // the view-space position behind this pixel (the far plane where there is only sky), and its slope across the
    // screen: outside every branch, as derivatives must be
    vec4 view4 = InverseProjMat * vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec3 viewPos = view4.xyz / view4.w;
    vec3 slopeX = dFdx(viewPos);
    vec3 slopeY = dFdy(viewPos);
    bool terrain = depth < 1.0;

    if (Mode > 0.5)
    {
        // --- the hole cover (see above) ---
        if (terrain || texture(NearDepthSampler, texCoord).x < 1.0)
        {
            fragColor = vec4(col, 1.0); // the ground, or a cloud of the near pass: no gap here
            return;
        }
        vec2 ring = CoverRadius * vec2(OutSize.y / OutSize.x, 1.0);
        vec3 sum = vec3(0.0);
        int hits = 0;
        for (int i = 0; i < 16; i++)
        {
            float a = float(i) * 0.3927;
            vec2 uv = clamp(texCoord + vec2(cos(a), sin(a)) * ring * ((i & 1) == 0 ? 1.0 : 0.6), vec2(0.0), vec2(1.0));
            if (texture(DiffuseDepthSampler, uv).x >= 1.0 && texture(NearDepthSampler, uv).x < 1.0)
            {
                sum += texture(DiffuseSampler, uv).rgb;
                hits++;
            }
        }
        if (hits >= 11)
            col = sum / float(hits);
        fragColor = vec4(col, 1.0);
        return;
    }

    // what lies behind a translucent block (see the uniform note)
    if (GlassOn > 0.5 && Strength > 0.0)
    {
        float opaque = texture(OpaqueDepthSampler, texCoord).x;
        vec4 pane = texture(GlassFilterSampler, texCoord);
        if (depth < opaque && pane.a > 0.02)
        {
            vec4 behind4 = InverseProjMat * vec4(texCoord * 2.0 - 1.0, opaque * 2.0 - 1.0, 1.0);
            vec3 behindPos = behind4.xyz / behind4.w;
            float lodBehind = opaque < 1.0 ? -1.0 : lodDistance();
            float haze = opaque < 1.0 ? groundHaze(length(behindPos)) : lodBehind >= 0.0 ? groundHaze(lodBehind) : skyHaze(behindPos);
            col = mix((col - pane.rgb) / pane.a, FogColor, haze) * pane.a + pane.rgb;
        }
    }

    float dist = length(viewPos);
    if (!terrain)
    {
        dist = lodDistance();
        if (dist < 0.0)
        {
            if (Strength > 0.0)
                col = mix(col, FogColor, skyHaze(viewPos));
            fragColor = vec4(col, 1.0);
            return;
        }
    }

    // --- wet ground ---
    if (terrain && Wetness > 0.0 && dist < WetRange)
    {
        vec3 normal = foreverfixesFaceNormal(viewPos); // Forever Fixes: was normalize(cross(slopeX, slopeY))
        if (dot(normal, UpView) > 0.85) // Forever Fixes: was abs(...) - undersides facing straight down (Sable ships, which the
                                         // terrain heightmap does not know, so they pass the open-sky test) got wet too
        {
            vec3 world = CameraPos + (InverseModelViewMat * vec4(viewPos, 1.0)).xyz;
            vec2 hm = (world.xz - HeightmapOrigin) / HeightmapSize;
            float open = all(greaterThanEqual(hm, vec2(0.0))) && all(lessThan(hm, vec2(1.0))) ? exposure(world.xz, world.y) : 0.0;
            if (open > 0.001)
            {
                float wet = Wetness * open * (1.0 - smoothstep(WetRange * 0.66, WetRange, dist));
                // puddles where the ground's noise dips, more of them the wetter it is, soft-edged as water
                // creeping over uneven ground is
                float noise = vnoise(world.xz * 0.35) * 0.7 + vnoise(world.xz * 0.9 + 17.3) * 0.3;
                float puddle = smoothstep(0.58, 0.74, noise + 0.14 * Wetness - 0.10) * wet;
                // wet ground is darker and a little cooler; under standing water darker still: the water is
                // seen through, the ground's own texture stays
                vec3 damp = mix(col, col * vec3(0.68, 0.70, 0.74), wet * 0.8);
                damp = mix(damp, damp * vec3(0.62, 0.64, 0.68), puddle);
                // the mirror: water gives back a few percent of the sky seen from above and most of it at a
                // grazing angle; the film on wet ground does so weakly and diffusely, a puddle's still surface
                // outright, and a ring from a raindrop tilts the surface out of the mirror
                float cosTheta = clamp(dot(normalize(-viewPos), UpView), 0.0, 1.0);
                float fresnel = 0.03 + 0.97 * pow(1.0 - cosTheta, 4.0);
                float rings = puddle > 0.001 ? ripples(world.xz) : 0.0;
                float mirror = wet * (0.02 + 0.22 * fresnel) + puddle * (0.10 + 0.70 * fresnel) * (1.0 - 0.7 * rings);
                col = mix(damp, WetSkyColor, clamp(mirror, 0.0, 0.8)) + rings * puddle * 0.05;
            }
        }
    }

    // --- haze ---
    if (Strength > 0.0)
        col = mix(col, FogColor, groundHaze(dist));
    fragColor = vec4(col, 1.0);
}
