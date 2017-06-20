package proxy;

import data.TestData;
import data.TestWrapData;

/**
 * Created by think on 2017/4/13.
 */
public interface Subject {
	int helloWorld(int a, int b);

	String getConfString(int a, int b);

	String addSum();

	TestWrapData testWarpData(int a, String b);

	String testFourParam(String a, String b, String c, String d);

	void testCallback(int a, int b, WzCallback<Integer> callback);

	void testComplexCallback(int a, int b, WzCallback<String> callback);
}
