#version 150

// Forever Fixes: the depths Better Simple Clouds' storm fog and rain haze read, with the translucent particles at their own
// distance. A particle is wherever the finished depth (LiveDepth, after the particles) is nearer than the depth after the
// translucent blocks (GlassDepth, which also holds the beacon glow).
//  Mode 0: the storm fog's depth - Better Simple Clouds' snapshot from before the translucent blocks (OpaqueDepth, with the
//          beacon glow) and the particles in front of it; glass stays see-through, so the rain still shows behind windows.
//  Mode 1: the rain haze's depth - the finished depth with the beacon glow.

uniform sampler2D LiveDepth;
uniform sampler2D GlassDepth;
uniform sampler2D OpaqueDepth;
uniform float Mode;

out vec4 fragColor;

void main()
{
	ivec2 pixel = ivec2(gl_FragCoord.xy);
	float live = texelFetch(LiveDepth, pixel, 0).r;
	float glass = texelFetch(GlassDepth, pixel, 0).r;
	if (Mode < 0.5)
	{
		float opaque = texelFetch(OpaqueDepth, pixel, 0).r;
		gl_FragDepth = live < glass ? min(live, opaque) : opaque;
	}
	else
	{
		gl_FragDepth = min(live, glass);
	}
	fragColor = vec4(0.0);
}
