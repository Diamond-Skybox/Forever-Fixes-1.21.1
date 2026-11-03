// Forever Fixes: a copy of Better Simple Clouds 1.2.4's storm_fog.fsh, itself Better Simple Clouds' copy of Simple Clouds 0.7.3's
// storm_fog.fsh (Simple Clouds: PolyForm Perimeter License 1.0.1, https://polyformproject.org/licenses/perimeter/1.0.1;
// Better Simple Clouds' changes: MIT, falling-colud/better-simple-clouds). One change, marked "Forever Fixes": the edges of
// distant rain columns. The march samples at distances that grow with the square of the step (about 57 blocks apart 4000
// blocks out), each sample all in or all out of the rain and worth rayDepth * 0.0001 of opacity (0.4 at 4000 blocks). Along a
// column's edge a pixel's fog could only be 0, 40, 80 or 100 %, and drifting clouds moved the rain across those fixed
// samples, so the edge flickered in 4x4-pixel blocks (the fog is a quarter of the screen size), worst against the sky. Here
// every pixel's samples are offset by a fixed amount of its own, and a step the rain starts or stops in counts only the part
// of it that is in the rain (found by halving the step three times).

//Portions of this file are licensed under MIT

// Better Simple Clouds: a copy of Simple Clouds' storm_fog.fsh (the distant rain wall) with two changes, both marked:
//  1. the distance fade is a smooth ramp over FogStart..FogEnd instead of pow(x, 0.05), which dropped to nothing within
//     a few blocks of FogStart; the Java side (StormFogPostChainMixin) keeps that range inside the ray-march's reach,
//     so the rain wall fades out before the march ends instead of stopping dead in a hard circle;
//  2. the scene depth the march stops at is read in the convention it was written in (MicDepthReverse /
//     MicDepthRange), because under Simple Clouds' Distant Horizons pipeline the depth is DH's;
//  3. the ray direction is oriented down the view axis explicitly: with DH's reversed-Z projection the stock
//     "far minus near" ray pointed backwards and the rain wall appeared behind the camera, mirrored.
// Everything else is Simple Clouds' own. If Simple Clouds updates storm_fog.fsh, re-copy it and re-apply the marked blocks.

#version 430

// -- Lightning Flashes --

struct Lightning {
	vec3 Position;
	float Alpha;
};

layout(std430) readonly buffer LightningBolts {
	Lightning data[];
}
lightning;

uniform int TotalLightningBolts;

// ----------------------

uniform sampler2D ShadowMap;
uniform sampler2D ShadowMapColor;
uniform sampler2D DepthSampler;

uniform mat4 InverseWorldProjMat;
uniform mat4 InverseModelViewMat;
uniform mat4 ShadowProjMat;
uniform mat4 ShadowModelViewMat;

uniform vec3 CameraPos;

uniform float CutoffDistance;
uniform float FogStart;
uniform float FogEnd;

uniform vec3 ColorThreshold;
uniform float VerticalFade;
uniform vec3 ColorMultiplier;
uniform vec4 ColorModulator;

// === Better Simple Clouds: depth convention of DepthSampler (1 = reversed Z; 1 = clip Z in 0..1). 0/0 = standard GL. ===
uniform float MicDepthReverse = 0.0;
uniform float MicDepthRange = 0.0;
// === Better Simple Clouds: a second depth the march also stops at - the clouds, when DepthSampler holds only the
// world (the storm fog drawn after the level on the Distant Horizons pipeline). It may be in another convention and
// projection than the first, so it gets its own inverse projection and flags. Off at MicCloudEnabled = 0. ===
uniform sampler2D MicCloudDepth;
uniform mat4 MicCloudInvProj = mat4(1.0);
uniform float MicCloudEnabled = 0.0;
uniform float MicCloudReverse = 0.0;
uniform float MicCloudRange = 0.0;
// === Better Simple Clouds: a shader pack's fog on the rain (see clouds.fsh for the idea). Under a shader pack the
// clouds fade out with distance, into the frame the pack drew; the rain under them has to fade the same way or it is
// left hanging, dark, under clouds that have melted into the haze. The wall's own fade (FogStart..FogEnd, per step)
// cannot do it: a deep wall still accumulates to full density. So the finished column is faded by the distance at
// which the ray ENTERED the rain - where the cloud above it is - with the clouds' own curve. Off at 0. ===
uniform float MicShaderFog = 0.0;
uniform float MicShaderFogStart = 0.0;
uniform float MicShaderFogEnd = 0.0;

in vec2 texCoord;
in vec2 oneTexel;
out vec4 fragColor;

#define FOG 0 // 0 for no fog, 1 for fog
#define STEPS 200 //Total ray steps. More smaller steps means better accuracy and less artifacts
#define STEP_SIZE 40.0 //Size of each individual step
#define BG_COL vec4(0.0);

vec3 getRayDirection(vec2 screenUV)
{
	vec2 uv = screenUV * 2.0 - 1.0;
	vec4 near = vec4(uv, 0.0, 1.0);
	vec4 far = vec4(uv, 1.0, 1.0);
	near = InverseWorldProjMat * near;
	far = InverseWorldProjMat * far;
	near.xyz /= near.w;
	far.xyz /= far.w;
	// === Better Simple Clouds: under a reversed-Z projection (Distant Horizons) clip z = 1 is the NEAR plane and z = 0
	// the far one, so "far - near" points back INTO the camera and the whole rain wall was marched behind the viewer,
	// mirrored. The view looks down -Z whatever the depth convention, so orient the ray that way before leaving view
	// space. ===
	vec3 viewDir = far.xyz - near.xyz;
	if (viewDir.z > 0.0)
		viewDir = -viewDir;
	return normalize((InverseModelViewMat * vec4(viewDir, 0.0)).xyz);
}

vec4 shadowMapColorAt(vec3 pos)
{
	vec4 shadowMapPos = ShadowProjMat * ShadowModelViewMat * vec4(pos, 1.0);
	vec3 ndc = shadowMapPos.xyz / shadowMapPos.w;
	vec3 coord = ndc * 0.5 + 0.5;
	float shadowMapDepth = texture(ShadowMap, coord.xy).x;
	if (shadowMapDepth < 1.0 && shadowMapDepth < coord.z)
		return vec4(texture(ShadowMapColor, coord.xy).rgb, 1.0);
	return vec4(0.0);
}

//---------------------------------------------
// The MIT License
// Copyright © 2016 Inigo Quilez
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// https://www.shadertoy.com/view/4lcSRn
vec4 cylinderVerticalIntersect(in vec3 ro, in vec3 rd, float he, float ra)
{
    float k2 = 1.0        - rd.y*rd.y;
    float k1 = dot(ro,rd) - ro.y*rd.y;
    float k0 = dot(ro,ro) - ro.y*ro.y - ra*ra;

    float h = k1*k1 - k2*k0;
    if( h<0.0 ) return vec4(-1.0);
    h = sqrt(h);
    float t = (-k1-h)/k2;

    // body
    float y = ro.y + t*rd.y;
    if( y>-he && y<he ) return vec4( t, (ro + t*rd - vec3(0.0,y,0.0))/ra );

    // caps
    t = ( ((y<0.0)?-he:he) - ro.y)/rd.y;
    if( abs(k1+k2*t)<h ) return vec4( t, vec3(0.0,sign(y),0.0) );

    return vec4(-1.0);
}
//---------------------------------------------

float getNearestLightningBoltColorModifier(vec3 position)
{
	for (int i = 0; i < TotalLightningBolts; i++)
	{
		Lightning bolt = lightning.data[i];
		float dist = distance(bolt.Position.xz, position.xz);
		if (dist < 2000.0)
		{
			float distMul = clamp(2.0 - dist * 0.001, 0.0, 1.0);
			return 1.0 + bolt.Alpha * distMul;
		}
	}
	return 1.0;
}

vec3 screenToWorldPos(vec2 coord, float depth)
{
	vec3 ndc = vec3(coord * 2.0 - 1.0, depth);
  	vec4 view = InverseWorldProjMat * vec4(ndc, 1.0);
  	view.xyz /= view.w;
  	vec3 result = (InverseModelViewMat * view).xyz;
  	return result;
}

// === Better Simple Clouds: the distance to whatever the scene holds at this pixel, in the depth's own convention. A
// cleared depth (far value: 1 standard, 0 reversed) is open sky, where the march runs its full length. ===
float micDecodeDepth(float raw, float reverse, float range, mat4 invProj)
{
	if (reverse > 0.5 ? raw <= 0.0 : raw >= 1.0)
		return 1.0e9;
	float ndcZ = range > 0.5 ? raw : raw * 2.0 - 1.0;
	vec4 view = invProj * vec4(texCoord * 2.0 - 1.0, ndcZ, 1.0);
	if (abs(view.w) < 1.0e-9)
		return 1.0e9;
	return length(view.xyz / view.w);
}

float micSceneDepth()
{
	float depth = micDecodeDepth(texture(DepthSampler, texCoord).r, MicDepthReverse, MicDepthRange, InverseWorldProjMat);
	if (MicCloudEnabled > 0.5)
		depth = min(depth, micDecodeDepth(texture(MicCloudDepth, texCoord).r, MicCloudReverse, MicCloudRange, MicCloudInvProj));
	return depth;
}

// === Forever Fixes: the stock test for rain at a point, the march's distance at a (fractional) step, and this pixel's offset
// of its steps: interleaved gradient noise, fixed for the pixel and different from its neighbours. ===
bool foreverfixesRainAt(vec3 pos, out vec3 rgb)
{
	vec4 col = shadowMapColorAt(pos);
	rgb = col.rgb;
	return col.a > 0.0 && col.r <= ColorThreshold.r && col.g <= ColorThreshold.g && col.b <= ColorThreshold.b;
}

float foreverfixesStepDistance(float s)
{
	return 0.2 * s * s;
}

float foreverfixesStepOffset()
{
	return fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
}

void main()
{
	vec3 rayDir = getRayDirection(texCoord);
	vec4 rayStartDist = cylinderVerticalIntersect(vec3(0.0), rayDir, 500.0, CutoffDistance);
	vec3 point = CameraPos + rayDir;// * -rayStartDist.x;
	float sceneDepth = micSceneDepth();
	float rayDepth = distance(point, CameraPos);

	float density = 0.0;
	vec3 colorAccum = vec3(0.0);
	float fogSteps = 0.0;
	float micEntryDepth = 0.0; // Better Simple Clouds: how far away the ray first met rain

	// === Forever Fixes: offset steps, and partial steps where the rain starts or stops (see the top of the file). ===
	float stepOffset = foreverfixesStepOffset();
	bool wasRain = false;
	vec3 rainRgb = vec3(0.0);
    for (int i = 0; i < STEPS; i++)
    {
    	float s = float(i) + stepOffset;
    	point = CameraPos + rayDir * foreverfixesStepDistance(s);
    	rayDepth = distance(point, CameraPos);

    	//If at any point the ray intersects with any vertex in the scene, we stop
    	if (sceneDepth < rayDepth)
    		break;

    	//If there is a shadow at this point (alpha > 0) and the color is within our designated threshold, it is rain
    	vec3 rgb;
    	bool rain = foreverfixesRainAt(point, rgb);
    	float weight = rain ? 1.0 : 0.0;
    	if (i > 0 && rain != wasRain)
    	{
    		// the rain's edge lies between the previous step (s - 1) and this one: lo stays on the previous step's side
    		float lo = s - 1.0;
    		float hi = s;
    		for (int k = 0; k < 3; k++)
    		{
    			float mid = 0.5 * (lo + hi);
    			vec3 unused;
    			if (foreverfixesRainAt(CameraPos + rayDir * foreverfixesStepDistance(mid), unused) == rain)
    				hi = mid;
    			else
    				lo = mid;
    		}
    		float edge = 0.5 * (lo + hi);
    		// entering: the part after the edge; leaving: the part before it, in the colour of the rain just left
    		weight = rain ? s - edge : edge - (s - 1.0);
    		if (!rain)
    			rgb = rainRgb;
    	}
    	wasRain = rain;
    	if (rain)
    		rainRgb = rgb;

    	if (weight > 0.0)
    	{
    		float densityAdd = rayDepth * 0.0001;

    		// === Better Simple Clouds: a smooth distance fade over FogStart..FogEnd, a range the Java side keeps inside
    		// the march's reach, so the wall thins out before the march ends instead of stopping dead. Simple Clouds'
    		// pow(x, 0.05) curve is all but a step at FogStart. ===
    		float fadeFactor = 1.0 - smoothstep(FogStart, FogEnd, rayDepth);

			// Vertical fade to prevent the storm fog from stretching down too far
    		fadeFactor *= clamp(1.0 + point.y / VerticalFade, 0.0, 1.0);

    		density += densityAdd * fadeFactor * weight;

    		if (fogSteps <= 0.0)
    			micEntryDepth = rayDepth;
    		fogSteps += weight;
			colorAccum += vec3(rgb * ColorMultiplier * ColorModulator.rgb) * weight;
    	}

    	if (density >= 1.0)
    		break;
    }
	// === end Forever Fixes ===

    if (fogSteps <= 0.0)
    {
    	fragColor = BG_COL;
    	return;
    }

    vec3 avgCol = colorAccum / fogSteps;
    vec4 finalCol = vec4(avgCol, min(density, 1.0));

    // === Better Simple Clouds: a shader pack's fog (see the uniform note). ===
    if (MicShaderFog > 0.5 && MicShaderFogEnd > MicShaderFogStart)
    	finalCol.a *= 1.0 - smoothstep(MicShaderFogStart, MicShaderFogEnd, micEntryDepth);

    // This is technically not correct but looks ok
    float lightningMul = getNearestLightningBoltColorModifier(point);
	finalCol.rgb *= lightningMul;

	fragColor = finalCol;
}
