package network.facade;


import facade.ITest;

/**
 * Created by think on 2017/5/18.
 */
public class TestImpl implements ITest {
	@Override
	public int testAdd(int a, int b) {
		return a + b;
	}
}
