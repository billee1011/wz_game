package proxy;

import data.TestData;
import data.TestWrapData;

/**
 * Created by think on 2017/4/13.
 */
public class RealObject implements Subject {
	@Override
	public int helloWorld(int a, int b) {
		return 1 + 2;
	}

	@Override
	public String getConfString(int a, int b) {
		if (a + b > 5) {
			return "the result gt 5";
		} else {
			return "the result lt 5";
		}
	}

	@Override
	public void testCallback(int a, int b, WzCallback<Integer> callback) {
		callback.onResult(a + b);
	}

	@Override
	public void testComplexCallback(int a, int b, WzCallback<String> callback) {
		String originalStr = "this is the original";

		callback.onResult(originalStr + (a + b));
	}

	public TestWrapData testWarpData(int a, String b) {
		TestData data = new TestData();
		data.setA(a);
		data.setB(b);
		TestWrapData result = new TestWrapData();
		result.setData(data);
		result.setA(11111);
		return result;
	}

	@Override
	public String testFourParam(String a, String b, String c, String d) {
		StringBuilder builder = new StringBuilder();
		builder.append("a;" + a);
		builder.append("b:" + b);
		builder.append("c:" + c);
		builder.append("d:" + d);
		return builder.toString();
	}

	@Override
	public String addSum() {
		return "just for fun";
	}
}
