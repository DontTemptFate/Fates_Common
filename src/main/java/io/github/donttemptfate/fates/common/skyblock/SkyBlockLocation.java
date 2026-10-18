package io.github.donttemptfate.fates.common.skyblock;

import io.github.donttemptfate.fates.common.text.LegacyText;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.Locale;

/**
 * Where the player is in SkyBlock, read from the sidebar once a second: in SkyBlock or not, and
 * inside a dungeon / Kuudra run. Only reads what's on screen. Deliberately not Hypixel's Mod API
 * (a mod that registers its payloads twice crashes the game). Whether the server is Hypixel at all
 * is each mod's own check.
 */
public final class SkyBlockLocation {
	private SkyBlockLocation() {}

	public record Where(boolean skyBlock, boolean instance) {
		public static final Where NONE = new Where(false, false);
	}

	private static volatile Where current = Where.NONE;
	private static volatile boolean readable = true;
	private static int ticks;

	/** Last reading (updated once a second). */
	public static Where current() {
		return current;
	}

	/** False when the sidebar couldn't be read at all (then don't block anything on it). */
	public static boolean readable() {
		return readable;
	}

	public static void tick(Minecraft mc) {
		if (++ticks % 20 != 0) return;
		try {
			current = mc.level == null ? Where.NONE : read(mc.level.getScoreboard());
			readable = true;
		} catch (RuntimeException | LinkageError e) {
			readable = false;
		}
	}

	public static Where read(Scoreboard sb) {
		Objective obj = sb.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (obj == null) return Where.NONE;
		String title = LegacyText.strip(obj.getDisplayName().getString()).toUpperCase(Locale.ROOT);
		boolean skyBlock = title.contains("SKYBLOCK") || title.contains("SKIBLOCK");
		boolean instance = false;
		if (skyBlock) {
			for (PlayerScoreEntry e : sb.listPlayerScores(obj)) {
				if (e.isHidden()) continue;
				Component line = PlayerTeam.formatNameForTeam(sb.getPlayersTeam(e.owner()), e.ownerName());
				if (isInstanceLine(line.getString())) {
					instance = true;
					break;
				}
			}
		}
		return new Where(skyBlock, instance);
	}

	/** Sidebar lines Hypixel shows in a dungeon ("The Catacombs (F7)") or Kuudra ("Kuudra's Hollow"). */
	public static boolean isInstanceLine(String raw) {
		String p = LegacyText.strip(raw).toLowerCase(Locale.ROOT);
		return p.contains("the catacombs") || p.contains("kuudra's hollow") || p.contains("master mode catacombs");
	}
}
