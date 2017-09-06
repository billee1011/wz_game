package service;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by win7 on 2017/5/27.
 */
public class T {
    public static void main(String args[]){
       Queue q = new PriorityQueue<Integer>();
        q.add(1);
        q.add(2);
        System.out.print(q.peek());
    }
}
