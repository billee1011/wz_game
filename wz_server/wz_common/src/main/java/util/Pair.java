package util;

/**
 * Created by WZ on 2016/8/25.
 */
public class Pair<E, F> {
	private E left;

	private F right;

	public Pair(E left, F right) {
		this.left = left;
		this.right = right;
	}

	public E getLeft() {
		return this.left;
	}

	public F getRight() {
		return this.right;
	}


}
