package util;

import java.security.SecureRandom;
import java.util.Random;

public class Randomizer {
	private static Randomizer instance = new Randomizer();
	private SecureRandom secureRandom;
	private Random rand;

	public static Randomizer getInst() {
		return instance;
	}

	private Randomizer() {
		secureRandom = new SecureRandom();
		rand = new Random(secureRandom.nextLong());
	}

	public static int nextInt() {
		int ret = getInst().rand.nextInt();
		if (ret < 0) {
			ret = -ret;
		}
		return ret;
	}

	public static boolean randomOk(int seed, int ratio) {
		if (seed <= 0) {
			throw new IllegalArgumentException("the seed of random can't be zero");
		}
		return nextInt(seed) < ratio;
	}


	public static int nextInt(int min, int max) {
		if (max == min) {
			return min;
		}
		return min + nextInt(max - min);
	}

	public static int nextInt(int mod) {
		int ret = getInst().rand.nextInt(mod);
		return ret;
	}

	public static double nextDouble() {
		return getInst().rand.nextDouble();
	}

	public static Random getRandom() {
		return getInst().rand;
	}

	public static double nextGaussian() {
		return getInst().rand.nextGaussian();
	}
}
