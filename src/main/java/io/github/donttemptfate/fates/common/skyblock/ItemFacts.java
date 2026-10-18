package io.github.donttemptfate.fates.common.skyblock;

import io.github.donttemptfate.fates.common.text.LegacyText;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * What a SkyBlock item says about itself, worked out once per item object (tooltips are drawn every
 * frame; copying the item's data and re-reading its lore each frame was the costly part). Read-only:
 * never change {@link #data()}.
 *
 * @param data       the item's custom data (SkyBlock's ExtraAttributes) when it has an {@code id}, else null
 * @param lore       the item's lore lines as Hypixel sent them
 * @param plain      the same lines without colour codes
 * @param listingCut first line of Hypixel's auction listing lines (Auctions Browser), or -1
 * @param cacheKey   same data, name, own lore and count → same key; null for non-SkyBlock items
 */
public record ItemFacts(CompoundTag data, List<Component> lore, List<String> plain, int listingCut, String cacheKey) {

	/** The item's own lore: without Hypixel's auction listing lines. */
	public List<Component> ownLore() {
		return listingCut < 0 ? lore : lore.subList(0, listingCut);
	}

	public String id() {
		return data == null ? null : data.getStringOr("id", null);
	}

	public String uuid() {
		return data == null ? null : data.getStringOr("uuid", null);
	}

	private record Cached(int count, Object customData, Object lore, Object name, ItemFacts facts) {}

	/** Weak keys: an item Hypixel replaced is forgotten with it. ItemStack compares by identity. */
	private static final Map<ItemStack, Cached> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

	/** The facts for an item (cached per item object; recomputed when its count, data, lore or name change). */
	public static ItemFacts of(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		Object cd = stack.get(DataComponents.CUSTOM_DATA);
		Object lr = stack.get(DataComponents.LORE);
		Object nm = stack.get(DataComponents.CUSTOM_NAME);
		Cached c = CACHE.get(stack);
		if (c != null && c.count == stack.getCount() && c.customData == cd && c.lore == lr && c.name == nm) return c.facts;
		ItemFacts f = compute(stack);
		CACHE.put(stack, new Cached(stack.getCount(), cd, lr, nm, f));
		return f;
	}

	private static ItemFacts compute(ItemStack stack) {
		CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag = cd == null ? null : cd.copyTag();
		if (tag != null && tag.getStringOr("id", "").isEmpty()) tag = null;
		ItemLore il = stack.get(DataComponents.LORE);
		List<Component> lore = il == null ? List.of() : il.lines();
		List<String> plain = plain(lore);
		int cut = LoreParts.listingStart(plain);
		String key = null;
		if (tag != null) {
			List<Component> own = cut < 0 ? lore : lore.subList(0, cut);
			int h = Objects.hash(tag, displayName(stack).getString(), own.stream().map(Component::getString).toList(), stack.getCount());
			key = "h:" + Integer.toHexString(h) + ":" + tag.getStringOr("id", "");
		}
		return new ItemFacts(tag, lore, plain, cut, key);
	}

	/** The item's custom name, else its normal name. */
	public static Component displayName(ItemStack stack) {
		Component custom = stack.get(DataComponents.CUSTOM_NAME);
		return custom != null ? custom : stack.getHoverName();
	}

	/** Lore lines as plain text (colour codes removed). */
	public static List<String> plain(List<Component> lines) {
		List<String> out = new ArrayList<>(lines.size());
		for (Component c : lines) out.add(LegacyText.strip(c.getString()));
		return out;
	}
}
