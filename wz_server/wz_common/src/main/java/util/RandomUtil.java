package util;

import java.util.Random;

/**
 * Created by WZ on 2016/8/25.
 */
public class RandomUtil {
	private static Random random = new Random();

	public static int randomInt(int seed) {
		return random.nextInt(seed);
	}

	public static int randomInt(int begin, int end) {
		return begin + random.nextInt(end - begin);
	}

	public static float randomFloat(float seed) {
		return random.nextFloat();
	}

}
