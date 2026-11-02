#version 150

// Forever Fixes: Simple Clouds' storm fog is ray-marched at a quarter of the screen size, each texel stopping at the scene
// depth found at its centre, and was stretched back with a plain linear blit: along every edge between something near
// and something far the far texels' fog bled onto the near pixels and the edge came out in 4-pixel steps. Here each
// screen pixel takes the fog of the low-resolution texels that marched against a distance like its own: a plain
// bilinear blend where the four nearest agree, the nearest-distance texel across an edge.

uniform sampler2D DiffuseSampler; // the storm fog, low resolution
uniform sampler2D DepthSampler;   // the depth the storm fog marched against, full resolution
uniform mat4 InverseProjMat;      // the storm fog's own inverse projection
uniform float DepthReverse;       // 1 = reversed Z
uniform float DepthRange;         // 1 = clip Z in 0..1
uniform vec2 InSize;
// Better Simple Clouds' second depth the march stops at (Distant Horizons' LOD terrain and the clouds), in its own
// convention and projection; off at CloudEnabled = 0
uniform sampler2D CloudDepth;
uniform mat4 CloudInverseProjMat;
uniform float CloudEnabled;
uniform float CloudReverse;
uniform float CloudRange;
// Better Simple Clouds' glass filter: what a translucent block adds (rgb) and lets through (alpha). The storm fog marched
// through the glass to what is behind it and is blended over the frame by its alpha, which put the fog in front of the glass;
// with the colour  glass + fog * (1 - coverage)  the blend gives the glass in front of the fog.
uniform sampler2D GlassFilter;
uniform float GlassOn;

in vec2 texCoord;
out vec4 fragColor;

// relative distance difference above which two samples lie on different sides of an edge
const float EDGE = 0.1;

// the storm fog's own reading of a depth (Better Simple Clouds' micDecodeDepth): open sky is far away
float decode(vec2 uv, float raw, float reverse, float range, mat4 inverse)
{
	if (reverse > 0.5 ? raw <= 0.0 : raw >= 1.0)
		return 1.0e9;
	float ndcZ = range > 0.5 ? raw : raw * 2.0 - 1.0;
	vec4 view = inverse * vec4(uv * 2.0 - 1.0, ndcZ, 1.0);
	if (abs(view.w) < 1.0e-9)
		return 1.0e9;
	return length(view.xyz / view.w);
}

// where the storm fog's march stops (micSceneDepth): the nearer of the scene and the second depth
float distanceAt(vec2 uv, float scene, float cloud)
{
	float d = decode(uv, scene, DepthReverse, DepthRange, InverseProjMat);
	if (CloudEnabled > 0.5)
		d = min(d, decode(uv, cloud, CloudReverse, CloudRange, CloudInverseProjMat));
	return d;
}

float difference(float a, float b)
{
	return abs(a - b) / max(min(a, b), 1.0e-3);
}

void main()
{
	ivec2 pixel = ivec2(gl_FragCoord.xy);
	float here = distanceAt(texCoord, texelFetch(DepthSampler, pixel, 0).r, texelFetch(CloudDepth, pixel, 0).r);
	vec2 position = texCoord * InSize - 0.5;
	vec2 base = floor(position);
	vec2 f = position - base;
	ivec2 last = ivec2(InSize) - 1;

	vec4 blended = vec4(0.0);
	vec4 nearest = vec4(0.0);
	float nearestDifference = 1.0e30;
	float widest = 0.0;
	for (int i = 0; i < 4; i++)
	{
		ivec2 offset = ivec2(i & 1, i >> 1);
		ivec2 texel = clamp(ivec2(base) + offset, ivec2(0), last);
		vec2 centre = (vec2(texel) + 0.5) / InSize;
		// where this texel's march stopped: the depth at its centre, as the storm fog sampled it
		float d = difference(distanceAt(centre, texture(DepthSampler, centre).r, texture(CloudDepth, centre).r), here);
		vec4 fog = texelFetch(DiffuseSampler, texel, 0);
		blended += fog * (offset.x == 1 ? f.x : 1.0 - f.x) * (offset.y == 1 ? f.y : 1.0 - f.y);
		if (d < nearestDifference)
		{
			nearestDifference = d;
			nearest = fog;
		}
		widest = max(widest, d);
	}
	vec4 fog = widest < EDGE ? blended : nearest;
	if (GlassOn > 0.5)
	{
		vec4 pane = texelFetch(GlassFilter, pixel, 0);
		fog.rgb = fog.rgb * pane.a + pane.rgb;
	}
	fragColor = fog;
}
