package util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Queue<E> {
    
    private InnerQueue<E> queue = null;
    private Semaphore semExist  = null;
    private Semaphore semEmpty = null;
    private Object mtxQue = null;
    private int maxNodeNum = 0;

    public Queue() {
        maxNodeNum = 0;
    }
    
    public boolean init(int maxNodeNum) {
        this.maxNodeNum = maxNodeNum;
        queue = new InnerQueue<E>();
        if (false == queue.Init(this.maxNodeNum)) {
            return false;
        }
        //
        semExist = new Semaphore(0);
        semEmpty = new Semaphore(this.maxNodeNum);
        mtxQue = new Object();
        if (semExist == null || semEmpty == null || mtxQue == null) {
            return false;
        }
        return true;
    }

    public boolean unInit() {
        queue.Uninit();
        return true;
    }

    public boolean waitTillPush(E node) {
        while (true)
        {
            semEmpty.acquireUninterruptibly();
            break;
        }
        synchronized (mtxQue) {
            queue.Push(node);
            semExist.release();
        }
        return true;
    }

    public E waitTillPop() {
        while (true)
        {
            semExist.acquireUninterruptibly();
            break;
        }
        synchronized (mtxQue) {
            E node = queue.Front();
            queue.Pop();
            semEmpty.release();
            return node;
        }
    }
    
    public boolean isEmpty()
    {
        return (queue.Size() == 0);
    }

    public int size() {
        return queue.Size();
    }
    
    private class InnerQueue<E> {
        private List<E> itemList = null;
        private int maxSize = 0;
        private int size = 0;
        private int begin = 0;
        private int end = 0;

        public InnerQueue() {
            itemList = null;
            maxSize = 0;
            size = 0;
            begin = 0;
            end = 0;
        }

        public boolean Init(int _maxSize) {
            if (0 == _maxSize) {
                return false;
            }
            maxSize = _maxSize;
            itemList = new ArrayList<E>();
            for (int i = 0; i < maxSize; i++) {
                itemList.add(null);
            }
            size = 0;
            begin = 0;
            end = 0;
            return true;
        }

        public boolean Uninit() {
            maxSize = 0;
            if (itemList != null) {
                itemList = null;
            }
            size = 0;
            begin = 0;
            end = 0;
            return true;
        }

        public E Front() {
            if (0 == size) {
                return null;
            }
            E node = itemList.get(begin);
            return node;
        }

        public boolean Push(E node) {
            if (size >= maxSize) {
                return false;
            }
            itemList.set(end, node);
            end++;
            size++;
            if (end >= maxSize) {
                end = 0;
            }
            return true;
        }

        public boolean Pop() {
            if (0 == size) {
                return false;
            }
            itemList.set(begin, null);
            begin++;
            size--;
            if (begin >= maxSize) {
                begin = 0;
            }
            return true;
        }

        public int Size() {
            return size;
        }
    }
}
