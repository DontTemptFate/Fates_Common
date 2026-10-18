package io.github.donttemptfate.fatescommon;

import io.github.donttemptfate.fatescommon.skyblock.SkyBlockLocation;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Fates Common: the library shared by the Fates mods (Fates Flipper, Fates OFA). Each mod bundles it
 * (jar-in-jar); with several installed, Fabric Loader keeps only the newest copy, so it's loaded once.
 */
public final class FatesCommon implements ClientModInitializer {
	public static final String MOD_ID = "fatescommon";

	@Override
	public void onInitializeClient() {
		Settings.init();
		ClientTickEvents.END_CLIENT_TICK.register(SkyBlockLocation::tick);
	}
}
