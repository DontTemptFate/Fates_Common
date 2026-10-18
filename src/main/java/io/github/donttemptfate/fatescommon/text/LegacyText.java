package io.github.donttemptfate.fatescommon.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text with legacy Minecraft colour codes (§a, §l, …) into styled runs.
 *
 * <p>Only colours and formatting are understood. Nothing in the text can create click or hover
 * actions: those are added by the mod itself, never taken from server data.
 */
public final class LegacyText {
	private LegacyText() {}

	/** Longest text accepted from the server for one chat line. */
	public static final int MAX_CHARS = 600;

	/**
	 * One styled piece of text. {@code color} is the code character (0-9, a-f) or 0 for the
	 * default colour.
	 */
	public record Run(String text, char color, boolean bold, boolean italic, boolean underlined,
			boolean strikethrough, boolean obfuscated) {}

	public static List<Run> parse(String input) {
		List<Run> runs = new ArrayList<>();
		if (input == null || input.isEmpty()) return runs;
		String s = sanitize(input);
		StringBuilder cur = new StringBuilder();
		char color = 0;
		boolean bold = false, italic = false, underlined = false, strike = false, obf = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '§' && i + 1 < s.length()) {
				char code = Character.toLowerCase(s.charAt(i + 1));
				boolean isColor = (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
				boolean isFormat = "klmnor".indexOf(code) >= 0;
				if (isColor || isFormat) {
					if (cur.length() > 0) {
						runs.add(new Run(cur.toString(), color, bold, italic, underlined, strike, obf));
						cur.setLength(0);
					}
					if (isColor) {
						color = code;
						bold = italic = underlined = strike = obf = false;
					} else {
						switch (code) {
							case 'k' -> obf = true;
							case 'l' -> bold = true;
							case 'm' -> strike = true;
							case 'n' -> underlined = true;
							case 'o' -> italic = true;
							case 'r' -> {
								color = 0;
								bold = italic = underlined = strike = obf = false;
							}
							default -> {
							}
						}
					}
					i++;
					continue;
				}
			}
			if (c == '§') continue; // stray section sign at the end
			cur.append(c);
		}
		if (cur.length() > 0) runs.add(new Run(cur.toString(), color, bold, italic, underlined, strike, obf));
		return runs;
	}

	/** Removes all colour codes. */
	public static String strip(String input) {
		StringBuilder sb = new StringBuilder();
		for (Run r : parse(input)) sb.append(r.text());
		return sb.toString();
	}

	/** One line, no control characters, bounded length. */
	public static String sanitize(String input) {
		StringBuilder sb = new StringBuilder(Math.min(input.length(), MAX_CHARS));
		for (int i = 0; i < input.length() && sb.length() < MAX_CHARS; i++) {
			char c = input.charAt(i);
			if (c == '\n' || c == '\r' || c == '\t') sb.append(' ');
			else if (c < 0x20 || c == 0x7f) continue;
			else sb.append(c);
		}
		return sb.toString();
	}

	// ---- from Fates OFA's Strip (0.3.0): cleaning text read from Hypixel's scoreboard, tab list and lore ----

	/** Removes §x formatting codes (and a dangling § at the end). */
	public static String codes(String s) {
		if (s == null) return "";
		StringBuilder b = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '§') {
				i++; // skip the code character
				continue;
			}
			b.append(c);
		}
		return b.toString();
	}

	/**
	 * Formatting codes removed, plus characters no SkyBlock parser needs: control characters,
	 * private-use glyphs (resource-pack icons), emoji and other supplementary characters, zero-width
	 * marks. Keeps letters, digits, punctuation and the symbols SkyBlock uses as markers
	 * (⏣ ф ❤ ❈ ✎ ☠ ✦ ⸕ ☘ ♲ Ⓑ ⚚ ✯ etc. are all in the Basic Multilingual Plane and kept).
	 */
	public static String clean(String s) {
		String t = codes(s);
		StringBuilder b = new StringBuilder(t.length());
		for (int i = 0; i < t.length(); ) {
			int cp = t.codePointAt(i);
			i += Character.charCount(cp);
			if (cp < 0x20 || cp == 0x7F) continue;
			if (cp >= 0xE000 && cp <= 0xF8FF) continue; // private use (resource pack glyphs)
			if (cp > 0xFFFF) continue; // emoji and other supplementary planes
			if (cp == 0x200B || cp == 0x200C || cp == 0x200D || cp == 0xFEFF || cp == 0x2060) continue;
			b.appendCodePoint(cp);
		}
		return b.toString().replaceAll("\\s+", " ").trim();
	}

	/** Lower-cases and removes everything but letters and digits: "Crystal Hollows" → "crystalhollows". */
	public static String key(String s) {
		return clean(s).toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
