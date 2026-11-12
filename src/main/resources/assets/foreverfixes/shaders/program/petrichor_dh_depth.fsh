#version 150

// Forever Fixes: the depth Petrichor's rain haze reads, with Distant Horizons' LOD terrain in it. Where Minecraft's depth is
// empty (sky) and Distant Horizons drew terrain, the LOD depth is turned into the depth Minecraft's projection gives the same
// point: unprojected with Distant Horizons' projection into view space, projected again with Minecraft's. Points past
// Minecraft's far plane are kept just short of it, so they still count as terrain, not sky.

uniform sampler2D MainDepth;
uniform sampler2D DhDepth;
uniform mat4 DhInvProj;
uniform mat4 McProj;
uniform float DhReverseZ;
uniform float DhZeroToOne;

in vec2 texCoord;

out vec4 fragColor;

void main()
{
	float mainDepth = texture(MainDepth, texCoord).r;
	fragColor = vec4(0.0);
	if (mainDepth < 0.99999)
	{
		gl_FragDepth = mainDepth;
		return;
	}
	float dh = texture(DhDepth, texCoord).r;
	bool empty = DhReverseZ > 0.5 ? dh <= 1.0e-7 : dh >= 0.9999999;
	if (empty)
	{
		gl_FragDepth = mainDepth;
		return;
	}
	float ndcZ = DhZeroToOne > 0.5 ? dh : dh * 2.0 - 1.0;
	vec4 view = DhInvProj * vec4(texCoord * 2.0 - 1.0, ndcZ, 1.0);
	view /= view.w;
	vec4 clip = McProj * view;
	float depth = clip.z / clip.w * 0.5 + 0.5;
	gl_FragDepth = clamp(depth, 0.0, 0.99998);
}
