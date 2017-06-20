package cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.reflect.FastClass;

import java.lang.reflect.Method;

/**
 * Created by think on 2017/4/17.
 */
public class CglibTesty {

	private int a;

	public int getA() {
		return a;
	}

	public int getB() {
		return 200;
	}

	public void setA(int a) {
		this.a = a;
	}

	public static void main(String[] args) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(CglibTesty.class);
		CglibTesty test = (CglibTesty) enhancer.create();

		System.out.println(test);
	}
}
