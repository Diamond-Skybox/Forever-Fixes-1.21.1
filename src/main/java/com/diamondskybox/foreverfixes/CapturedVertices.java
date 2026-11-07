package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Records already-transformed vertices so they can be written later, buffer after buffer. A vanilla buffer source keeps only one
 * non-fixed buffer open at a time (asking for another ends the first), so geometry meant for two textures (MCA skin + clothing) is
 * captured while the caller draws and replayed into each texture's buffer once it is done.
 */
public final class CapturedVertices implements VertexConsumer
{
	// x, y, z, r, g, b, a, u, v, overlayU, overlayV, lightU, lightV, nx, ny, nz
	private final List<float[]> vertices = new ArrayList<>();
	private float[] current;

	@Override
	public VertexConsumer addVertex(float x, float y, float z)
	{
		current = new float[]{x, y, z, 255, 255, 255, 255, 0, 0, 0, 10, 240, 240, 0, 1, 0};
		vertices.add(current);
		return this;
	}

	@Override
	public VertexConsumer setColor(int r, int g, int b, int a)
	{
		current[3] = r;
		current[4] = g;
		current[5] = b;
		current[6] = a;
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v)
	{
		current[7] = u;
		current[8] = v;
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v)
	{
		current[9] = u;
		current[10] = v;
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v)
	{
		current[11] = u;
		current[12] = v;
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z)
	{
		current[13] = x;
		current[14] = y;
		current[15] = z;
		return this;
	}

	public boolean isEmpty()
	{
		return vertices.isEmpty();
	}

	/** Writes every captured vertex to the buffer, colours multiplied by the ARGB tint. */
	public void replay(VertexConsumer out, int tint)
	{
		final int ta = tint >>> 24 & 0xFF, tr = tint >> 16 & 0xFF, tg = tint >> 8 & 0xFF, tb = tint & 0xFF;
		for (float[] v : vertices)
		{
			out.addVertex(v[0], v[1], v[2])
					.setColor((int) v[3] * tr / 255, (int) v[4] * tg / 255, (int) v[5] * tb / 255, (int) v[6] * ta / 255)
					.setUv(v[7], v[8])
					.setUv1((int) v[9], (int) v[10])
					.setUv2((int) v[11], (int) v[12])
					.setNormal(v[13], v[14], v[15]);
		}
	}
}
