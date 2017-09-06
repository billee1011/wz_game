package logic.majiong.cpstragety;

import java.util.List;
import java.util.Queue;
import java.util.Stack;

public interface IStragetyHandler {
	public boolean handler(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain);
}
