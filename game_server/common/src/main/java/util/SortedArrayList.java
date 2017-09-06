package util;

import java.util.ArrayList;
import java.util.Comparator;

public class SortedArrayList<E> extends ArrayList<E>  {
    private Comparator<E> comparator = null;
    
    public SortedArrayList(Comparator<E> comparator) {
        super();
        this.comparator = comparator;
    }

    private int bsearch(int begin, int end, E e) {
        if (end < begin) {
            return begin - 1;
        }
        while (end >= begin) {
            int mid = (begin + end) / 2;
            int cmp = comparator.compare(get(mid), e);
            if (cmp == 0) {
                return mid;
            }
            else if (cmp < 0) {
                begin = mid + 1;
            }
            else {
                end = mid - 1;
            }
        }
        return begin - 1;
    }

    @Override
    public boolean add(E e) {
        if (size() <= 0) {
            super.add(e);
            return true;
        }
        // 1. find the pos
        int pos = bsearch(0, size() - 1, e);
        // 2. move the array
        this.add(pos + 1, e);
        return true;
    }
    
    public void clearNull() {
        int curr = 0;
        for (int i = 0; i < size(); i ++) {
            if (get(i) != null) {
                if (curr != i) {
                    set(curr, get(i));
                }
                curr ++;
            }
        }
        if (curr < size()) {
            removeRange(curr, size());
        }
    }

    public static void main(String args[]) {
        SortedArrayList<Integer> list = new SortedArrayList<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (o1 > o2) {
                    return  1;
                }
                else if (o1 < o2) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        });
        for (int i = 0; i < 100; i ++) {
            int rand = Randomizer.nextInt(1000);
            list.add(rand);
            System.out.println(rand);
        }
        System.out.println("==================size = " + list.size());
        for (int i = 0; i < list.size(); i ++) {
            if (i > 0) {
                if (list.get(i) < list.get(i - 1)) {
                    System.err.println("oshit!!!");
                    System.exit(0);
                }
            }
            System.out.println(list.get(i));
        }
    }
}

