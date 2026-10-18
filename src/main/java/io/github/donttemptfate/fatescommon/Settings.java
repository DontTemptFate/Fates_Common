package io.github.donttemptfate.fatescommon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.donttemptfate.fatescommon.ui.Theme;
import io.github.donttemptfate.fatescommon.ui.Ui;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * The settings every Fates mod shares, saved in {@code config/fatescommon.json}:
 * <pre>{"chakra": true, "theme": "fates"}</pre>
 * The library is loaded once however many Fates mods are installed, so there is one font and one
 * theme while the game runs; this file keeps them between games. Each mod's settings screen changes
 * them through here and says they're shared.
 */
public final class Settings {
	private Settings() {}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile Path file;
	private static volatile boolean chakra = true;
	private static volatile String theme = Theme.FATES.id();
	private static volatile boolean existed;

	/** Reads the file (missing or broken → defaults) and applies it. Called once by the library at start. */
	public static synchronized void load(Path path) {
		file = path;
		existed = false;
		try {
			if (path != null && Files.isRegularFile(path) && Files.size(path) < 64 * 1024) {
				JsonObject o = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
				if (o.has("chakra") && o.get("chakra").isJsonPrimitive()) chakra = o.get("chakra").getAsBoolean();
				if (o.has("theme") && o.get("theme").isJsonPrimitive()) theme = Theme.byId(o.get("theme").getAsString()).id();
				existed = true;
			}
		} catch (Exception ignored) {
			// broken file: defaults, rewritten on the next change
		}
		apply();
	}

	/** False until the file has been written once (lets a mod carry over its own older setting). */
	public static boolean saved() {
		return existed;
	}

	public static boolean chakra() {
		return chakra;
	}

	public static Theme theme() {
		return Theme.byId(theme);
	}

	public static synchronized void setChakra(boolean on) {
		chakra = on;
		apply();
		save();
	}

	public static synchronized void setTheme(Theme t) {
		theme = (t == null ? Theme.FATES : t).id();
		apply();
		save();
	}

	private static void apply() {
		Ui.useChakra(chakra);
		Ui.theme(Theme.byId(theme));
	}

	private static void save() {
		Path f = file;
		if (f == null) return;
		try {
			JsonObject o = new JsonObject();
			o.addProperty("chakra", chakra);
			o.addProperty("theme", theme);
			Files.createDirectories(f.getParent());
			Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
			Files.writeString(tmp, GSON.toJson(o), StandardCharsets.UTF_8);
			Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
			existed = true;
		} catch (Exception ignored) {
			// not saved: still applied for this game
		}
	}
}
