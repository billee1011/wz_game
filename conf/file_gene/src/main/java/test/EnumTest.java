package test;

/**
 * Created by think on 2017/4/8.
 */
public class EnumTest {

	public static void main(String[] args) {
		EItemQuality quality = EItemQuality.valueOf("BLUE");
		System.out.println("the quality value is " + quality.getValue());
	}
}
