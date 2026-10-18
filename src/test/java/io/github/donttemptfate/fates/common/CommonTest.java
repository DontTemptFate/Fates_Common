package io.github.donttemptfate.fates.common;

import io.github.donttemptfate.fates.common.skyblock.LoreParts;
import io.github.donttemptfate.fates.common.text.Coins;
import io.github.donttemptfate.fates.common.text.LegacyText;
import io.github.donttemptfate.fates.common.text.Versions;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/** Plain-Java parts of the library (no game needed). */
public class CommonTest {
	@Test
	public void legacyText() {
		List<LegacyText.Run> runs = LegacyText.parse("§6FLIP §lHyperion §r§7for §e1.2B");
		assertEquals(4, runs.size());
		assertEquals("FLIP ", runs.get(0).text());
		assertEquals('6', runs.get(0).color());
		assertEquals("Hyperion ", runs.get(1).text());
		assertTrue(runs.get(1).bold());
		assertEquals('6', runs.get(1).color());
		assertEquals("for ", runs.get(2).text());
		assertFalse(runs.get(2).bold());
		assertEquals('7', runs.get(2).color());
		assertEquals("FLIP Hyperion for 1.2B", LegacyText.strip("§6FLIP §lHyperion §r§7for §e1.2B"));
		// colour code resets bold; unknown codes stay as text; control chars and newlines removed
		assertEquals("a bzc", LegacyText.strip("a\nb§zc\u0007"));
		assertEquals(LegacyText.MAX_CHARS, LegacyText.strip("y".repeat(5000)).length());
		assertTrue(LegacyText.parse("§l§aX").get(0).color() == 'a' && !LegacyText.parse("§l§aX").get(0).bold());
		assertEquals("end", LegacyText.strip("end§"));
	}

	@Test
	public void coins() {
		assertEquals("950", Coins.format(950));
		assertEquals("12.5k", Coins.format(12_500));
		assertEquals("1.23M", Coins.format(1_234_567));
		assertEquals("100M", Coins.format(100_000_000));
		assertEquals("1.2B", Coins.format(1_200_000_000));
		assertEquals("-2M", Coins.format(-2_000_000));
		assertEquals("?", Coins.format(Double.NaN));
		assertEquals("45s", Coins.age(45_000));
		assertEquals("3m", Coins.age(185_000));
		assertEquals("2h 5m", Coins.age(7_500_000));
		assertEquals("34%", Coins.percent(0.34));
	}

	@Test
	public void loreListingFooter() {
		List<String> scroll = List.of("Consume this item to add its", "destination to your Fast Travel", "options.", "",
				"Requires MVP+ to consume!", "", "Island: Spider's Den", "Teleport: Top of Nest", "", "EPIC TRAVEL SCROLL",
				"-----------------", "Seller: [MVP+] Invisity_", "Buy it now: 1,000 coins", "", "Ends in: 05h 49m 49s", "",
				"Click to inspect!");
		assertEquals(10, LoreParts.listingStart(scroll));
		// the countdown ticking doesn't change where the item's own lore ends
		List<String> later = new java.util.ArrayList<>(scroll);
		later.set(14, "Ends in: 05h 49m 23s");
		assertEquals(scroll.subList(0, 10), later.subList(0, LoreParts.listingStart(later)));
		// auctions with bids, no separator: cut at the blank line above "Seller:"
		assertEquals(1, LoreParts.listingStart(List.of("RARE SWORD", "", "Seller: x", "Starting bid: 5 coins", "Bids: 3 bids")));
		// ordinary items keep everything, including progress bars and stat lines
		assertEquals(-1, LoreParts.listingStart(List.of("Damage: +270", "━━━━━━━━━━━━ 42%", "", "LEGENDARY SWORD")));
		assertEquals(-1, LoreParts.listingStart(List.of()));
		assertTrue(LoreParts.isSeparator("-----------------"));
		assertFalse(LoreParts.isSeparator("━━━━━━━━ 42.42%"));
	}

	@Test
	public void coinsRoundBeforeUnit() {
		assertEquals("1M", Coins.format(999_999.5));
		assertEquals("1M", Coins.format(999_950));
		assertEquals("1k", Coins.format(999.6));
		assertEquals("999", Coins.format(999.4));
		assertEquals("1B", Coins.format(999_999_999));
		assertEquals("1.5M", Coins.format(1_500_000));
		assertEquals("-2.5k", Coins.format(-2_500));
	}

	@Test
	public void versions() {
		assertTrue(Versions.compare("0.3.3", "0.3.4") < 0);
		assertTrue(Versions.compare("0.3.10", "0.3.4") > 0);
		assertTrue(Versions.compare("0.3.4-beta.2", "0.3.4") < 0);
		assertTrue(Versions.compare("0.3.4-beta.2", "0.3.4-beta.10") < 0);
		assertEquals(0, Versions.compare("0.3.4", "v0.3.4"));
		assertEquals(0, Versions.compare("garbage", "0.3.4")); // never blocks on an unreadable value
		assertEquals("0.3.4", Versions.plain("0.3.4+mc26.3"));
		assertEquals("https://modrinth.com/mod/x", Versions.safeUrl("https://modrinth.com/mod/x"));
		assertNull(Versions.safeUrl("http://evil.example"));
		assertNull(Versions.safeUrl("https://a b"));
	}

	@Test
	public void bazaarProducts() {
		assertTrue(LoreParts.isBazaarProduct(List.of("Common commodity", "", "Buy price: 2.5 coins")));
		assertTrue(LoreParts.isBazaarProduct(List.of("Buy price: 2.5 coins", "Sell price: 1.2 coins")));
		assertFalse(LoreParts.isBazaarProduct(List.of("Buy it now: 320 coins", "COMMON")));
	}
}
