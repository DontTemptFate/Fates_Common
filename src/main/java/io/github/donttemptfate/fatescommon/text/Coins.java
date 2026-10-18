package io.github.donttemptfate.fatescommon.text;

/**
 * The Flipper's original formatting names, kept so existing callers keep working. Same output as
 * before; new code uses {@link Fmt}.
 */
public final class Coins {
	private Coins() {}

	/** "1.23M", "950k", "12.5B"; rounds before choosing the unit (999,999.5 → "1M"). */
	public static String format(double coins) {
		return Fmt.coins(coins);
	}

	/** "12s", "4m", "2h 5m". */
	public static String age(long ms) {
		return Fmt.age(ms);
	}

	/** "34%". */
	public static String percent(double fraction) {
		return Fmt.percentWhole(fraction);
	}
}
