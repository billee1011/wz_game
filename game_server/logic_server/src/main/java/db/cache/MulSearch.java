package db.cache;

public class MulSearch<E> {

    public static class LinkNode<E> {
        public Number key = null;
        public E data = null;
        public LinkNode<E> next = null;
        public LinkNode<E> same_next = null;
    }

    ;

    private class SHashPort<E> {
        public int node_num = 0;
        public LinkNode<E> root = null;
    }

    ;

    // ===================================================
    private int hash_size = 0; // hash桶大小
    private int total_node_num = 0; // 插入的节点总数
    private SHashPort<E>[] ports = null; // hash 入口

    public MulSearch() {
        // do nothing
    }

    public boolean init(int hashSize) {
        if (0 >= hashSize) {
            return false;
        }
        // 5. hash port
        this.hash_size = hashSize;
        ports = new SHashPort[hash_size];
        for (int i = 0; i < hashSize; i++) {
            ports[i] = new SHashPort<E>();
            ports[i].node_num = 0;
            ports[i].root = null;
        }
        // succ
        return true;
    }

    public boolean AddKey(Number key, E data) {
        LinkNode<E> addNode = null;
        LinkNode<E> iterator = null;
        LinkNode<E> inside = null;
        int pos = 0;

        // 1. get new node
        addNode = new LinkNode<E>();
        addNode.key = key;
        addNode.data = data;
        addNode.next = null;
        addNode.same_next = null;
        // 2. find the root
        pos = (int) (key.longValue() % hash_size);
        iterator = ports[ pos].root;
        while (null != iterator) {
            if (key.equals(iterator.key)) {
                break;
            }
            iterator = iterator.next;
        }
        // 3. insert
        if (null != iterator) {
            inside = iterator;
            while (null != inside.same_next) {
                inside = inside.same_next;
            }
            inside.same_next = addNode;
        } else {
            addNode.next = ports[pos].root;
            ports[pos].root = addNode;
        }

        ports[pos].node_num++;
        total_node_num++;
        return true;
    }

    public boolean DeleteKey(Number key) {
        LinkNode<E> deleteHead = null;
        LinkNode<E> iterator = null;
        LinkNode<E> inside = null;
        int pos = 0;

        // 1. find
        pos = (int)(key.longValue() % hash_size);
        iterator = ports[pos].root;
        if (null == iterator) {
            return false;
        }
        if (key.equals(iterator.key)) {
            deleteHead = iterator;
            ports[pos].root = iterator.next;
            iterator.next = null;
        } else {
            while (null != iterator.next) {
                if (key.equals(iterator.next.key)) {
                    break;
                }
                iterator = iterator.next;
            }
            if (null != iterator.next) {
                deleteHead = iterator.next;
                iterator.next = deleteHead.next;
                deleteHead.next = null;
            }
        }
        // 2. delete
        while (null != deleteHead) {
            inside = deleteHead;
            deleteHead = deleteHead.same_next;

            inside.same_next = null;
            inside = null;

            total_node_num--;
            ports[pos].node_num--;
        }
        return true;
    }

    public LinkNode<E> Search(Number key) {
        LinkNode<E> iterator = null;
        int pos = 0;

        pos = (int)(key.longValue() % hash_size);
        iterator = ports[ pos].root;
        while (null != iterator) {
            if (key.equals(iterator.key)) {
                break;
            }
            iterator = iterator.next;
        }
        return iterator;
    }

    public int GetNodeNum() {
        return total_node_num;
    }
}
