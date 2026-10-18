package io.github.donttemptfate.fatescommon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.donttemptfate.fatescommon.skyblock.ItemKey;
import io.github.donttemptfate.fatescommon.skyblock.Rarity;
import io.github.donttemptfate.fatescommon.text.Coins;
import io.github.donttemptfate.fatescommon.text.Fmt;
import io.github.donttemptfate.fatescommon.text.LegacyText;
import io.github.donttemptfate.fatescommon.ui.Theme;
import io.github.donttemptfate.fatescommon.ui.Ui;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/** 0.3.0: the parts moved in from Fates OFA (its tests, with Strip → LegacyText), themes and shared settings. */
public class SharedTest {
	@Test
	public void shortNumbers() {
		assertEquals("999", Fmt.shortNum(999));
		assertEquals("1.2k", Fmt.shortNum(1_200));
		assertEquals("34k", Fmt.shortNum(34_000));
		assertEquals("1.25M", Fmt.shortNum(1_250_000));
		assertEquals("120M", Fmt.shortNum(120_000_000));
		assertEquals("1M", Fmt.shortNum(999_999));
		assertEquals("-2.5M", Fmt.shortNum(-2_500_000));
		assertEquals("+1.5k", Fmt.signedShort(1_500));
		assertEquals("1.4B", Fmt.shortNum(1_400_000_000));
	}

	@Test
	public void parsing() {
		assertEquals(1_234_567, Fmt.parse("1,234,567"), 0);
		assertEquals(1_200_000, Fmt.parse("1.2M"), 0.01);
		assertEquals(950_000, Fmt.parse("950k"), 0.01);
		assertTrue(Double.isNaN(Fmt.parse("abc")));
		assertEquals(14, Fmt.roman("XIV"));
		assertEquals(50, Fmt.roman("L"));
		assertEquals(0, Fmt.roman("Hello"));
		assertEquals(7, Fmt.level("VII"));
		assertEquals(23, Fmt.level("23"));
	}

	@Test
	public void durations() {
		assertEquals("45s", Fmt.duration(45_000));
		assertEquals("3m 05s", Fmt.duration(185_000));
		assertEquals("2h 05m", Fmt.duration(7_500_000));
		assertEquals("1:05", Fmt.clock(65_000));
		assertEquals("1:02:03", Fmt.clock(3_723_000));
	}

	/** Coins is now a pass-through to Fmt: same output as before 0.3.0. */
	@Test
	public void coinsMatchesFmt() {
		for (double v : new double[] {0, 999, 1_234, 1_250_000, 1.2e9, -3_400}) {
			assertEquals(Fmt.coins(v), Coins.format(v));
		}
		assertEquals(Fmt.age(90_000), Coins.age(90_000));
		assertEquals(Fmt.percentWhole(0.123), Coins.percent(0.123));
	}

	@Test
	public void stripping() {
		assertEquals("Purse: 1,234", LegacyText.clean("§6Purse: §61,234"));
		// emoji and private-use icons Hypixel inserts are dropped, SkyBlock symbols kept
		assertEquals("⏣ Hub", LegacyText.clean("§7⏣ §bHub👾"));
		assertEquals("Late Autumn 15th", LegacyText.clean(" Late Autumn 15th"));
		assertEquals("crystalhollows", LegacyText.key("§5Crystal Hollows"));
	}

	@Test
	public void itemKeys() {
		assertEquals("HYPERION", ItemKey.of(json("{id:'HYPERION',uuid:'x'}")));
		assertEquals("INK_SACK-3", ItemKey.of(json("{id:'INK_SACK:3'}")));
		assertEquals("ENDER_DRAGON;4", ItemKey.of(json("{id:'PET',petInfo:'{\"type\":\"ENDER_DRAGON\",\"tier\":\"LEGENDARY\"}'}")));
		assertEquals("ULTIMATE_WISE;5", ItemKey.of(json("{id:'ENCHANTED_BOOK',enchantments:{ultimate_wise:5}}")));
		assertEquals("ENCHANTED_BOOK", ItemKey.of(json("{id:'ENCHANTED_BOOK',enchantments:{sharpness:5,smite:5}}")));
		assertEquals("ZOMBIE_SLASH_RUNE;3", ItemKey.of(json("{id:'RUNE',runes:{ZOMBIE_SLASH:3}}")));
		assertNull(ItemKey.of(json("{foo:1}")));
		assertEquals("PET", ItemKey.of(json("{id:'PET',petInfo:'broken'}")));
	}

	@Test
	public void rarityLine() {
		assertEquals(Rarity.LEGENDARY, Rarity.fromLine("§6§lLEGENDARY SWORD"));
		assertEquals(Rarity.MYTHIC, Rarity.fromLine("§d§l§ka§r §d§lMYTHIC ACCESSORY §d§l§ka"));
		assertEquals(Rarity.VERY_SPECIAL, Rarity.fromLine("§c§lVERY SPECIAL HATCESSORY"));
		assertEquals(Rarity.COMMON, Rarity.fromLine("§f§lCOMMON"));
		assertNull(Rarity.fromLine("§7Damage: §c+100"));
	}

	@Test
	public void themes() {
		assertEquals(4, Theme.ALL.size());
		assertSame(Theme.FATES, Theme.byId("nonsense"));
		assertSame(Theme.FATES, Theme.byId(null));
		assertSame(Theme.EMBER, Theme.byId("ember"));
		assertFalse(Theme.VANILLA.chakra());
		// the Fates theme keeps the old constants' colours
		assertEquals(Ui.RULE, Theme.FATES.rule());
		assertEquals(Ui.TRACK, Theme.FATES.track());
		assertEquals(Ui.BAR, Theme.FATES.bar());
	}

	@Test
	public void settingsSaveAndLoad() throws Exception {
		Path dir = Files.createTempDirectory("fatescommon");
		Path f = dir.resolve("fatescommon.json");
		try {
			Settings.load(f); // missing file → defaults, nothing written yet
			assertFalse(Settings.saved());
			assertTrue(Settings.chakra());
			assertSame(Theme.FATES, Settings.theme());

			Settings.setTheme(Theme.MIDNIGHT);
			Settings.setChakra(false);
			assertTrue(Settings.saved());
			assertSame(Theme.MIDNIGHT, Ui.theme());
			assertFalse(Ui.chakra());

			Settings.load(f);
			assertSame(Theme.MIDNIGHT, Settings.theme());
			assertFalse(Settings.chakra());

			Files.writeString(f, "{broken");
			Settings.load(f); // broken file → keeps going
			Files.writeString(f, "{\"theme\":\"unknown\",\"chakra\":true}");
			Settings.load(f);
			assertSame(Theme.FATES, Settings.theme());
			assertTrue(Settings.chakra());
		} finally {
			Settings.load(null);
			Settings.setTheme(Theme.FATES);
			Settings.setChakra(true);
			Files.deleteIfExists(f);
			Files.deleteIfExists(dir);
		}
	}

	static JsonObject json(String s) {
		return JsonParser.parseString(s).getAsJsonObject();
	}
}
