package io.github.donttemptfate.fates.common.mixin;

import io.github.donttemptfate.fates.common.ui.DeferredTooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Lets a Fates card be this frame's tooltip, drawn last like vanilla's (see {@link DeferredTooltip}). */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin implements DeferredTooltip {
	@Shadow
	private Runnable deferredTooltip;

	@Override
	public void fatescommon$setDeferredTooltip(Runnable r) {
		this.deferredTooltip = r;
	}
}
