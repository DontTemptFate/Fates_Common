package io.github.donttemptfate.fatescommon.ui;

/** Implemented by a mixin on GuiGraphicsExtractor: sets this frame's tooltip (drawn last, on top). */
public interface DeferredTooltip {
	void fatescommon$setDeferredTooltip(Runnable r);
}
