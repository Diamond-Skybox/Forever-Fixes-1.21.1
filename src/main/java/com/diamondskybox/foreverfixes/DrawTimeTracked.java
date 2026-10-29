package com.diamondskybox.foreverfixes;

/** Added by mixin to NTGL's ItemAnimator: when it was last drawn (System.currentTimeMillis), 0 if never. */
public interface DrawTimeTracked
{
	long foreverfixes$lastDrawnMillis();
}
