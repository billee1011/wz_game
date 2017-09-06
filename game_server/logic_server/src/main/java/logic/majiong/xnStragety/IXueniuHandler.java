package logic.majiong.xnStragety;

import logic.majiong.XueniuFanType;

import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by Administrator on 2016/12/20.
 */
public interface IXueniuHandler {
	public boolean handler(List<XueniuFanType> typeList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain);
}
