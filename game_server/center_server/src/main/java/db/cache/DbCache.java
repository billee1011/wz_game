//package db.cache;
//
//import util.MiscUtil;
//
//import java.util.List;
//
//public class DbCache {
//    private class InnerCacheNode {
//        Number key = null; // key值
//        Object data = null; // 数据
//        boolean dirty = false; // 是否脏数据
//        InnerCacheNode prev = null; // lru前向节点
//        InnerCacheNode next = null; // lru后向节点
//        InnerCacheNode hash_next = null; // hash表的下一个
//        InnerCacheNode dirty_prev = null; // 脏数据链表的上一个
//        InnerCacheNode dirty_next = null; // 脏数据链表的下一个
//    };
//
//    // ===================================================================================================
//    // 双向链表操作
//    // 在Tail后加入cur当前节点
//    private static void DLIST_ADD(InnerCacheNode CUR, InnerCacheNode TAIL) {
//        CUR.next = TAIL.next;
//        CUR.prev = TAIL;
//        TAIL.next = CUR;
//        CUR.next.prev = CUR;
//    }
//
//    // 从双向链表中脱离cur节点
//    private static void DLIST_DEL(InnerCacheNode CUR) {
//        if (CUR.prev != null && CUR.next != null) {
//            CUR.prev.next = CUR.next;
//            CUR.next.prev = CUR.prev;
//            CUR.prev = CUR.next = null;
//        }
//    }
//
//    // 初始化双向链表的头
//    private static void DLIST_INIT_HEAD(InnerCacheNode H) {
//        H.next = H;
//        H.prev = H;
//    }
//
//    // 初始化节点双向指针为空
//    private static void DLIST_CUR_NULL(InnerCacheNode CUR) {
//        CUR.next = CUR.prev = null;
//    }
//
//    private static void DIRTY_ADD(InnerCacheNode CUR, InnerCacheNode TAIL) {
//        CUR.dirty_next = TAIL.dirty_next;
//        CUR.dirty_prev = TAIL;
//        TAIL.dirty_next = CUR;
//        CUR.dirty_next.dirty_prev = CUR;
//    }
//
//    private static void DIRTY_DEL(InnerCacheNode CUR) {
//        if (CUR.dirty_prev != null && CUR.dirty_next != null) {
//            CUR.dirty_prev.dirty_next = CUR.dirty_next;
//            CUR.dirty_next.dirty_prev = CUR.dirty_prev;
//            CUR.dirty_prev = CUR.dirty_next = null;
//        }
//    }
//
//    // =====================================================================================
//
//    private Object cache_mutex = null; // 锁
//    private InnerCacheNode[] hash_ports = null; // hash 头
//    private InnerCacheNode lru_head = null; // lru 头
//    private InnerCacheNode lru_mid = null; // lru中部
//    private InnerCacheNode dirty_head = null; // 脏数据链头
//    private InnerCacheNode mem_head = null; // 空闲内存头
//
//    private int curr_size = 0;
//    private int clean_size = 0;
//    private int dirty_size = 0;
//    private int hash_size = 0; // hash桶的大小
//    private int max_size = 0; // 数据节点最大数量
//    private int max_clean_size = 0; // 干净节点最大数量
//
//    public DbCache() {
//        cache_mutex = new Object();
//        hash_ports = null;
//        lru_head = null;
//        lru_mid = null;
//        dirty_head = null;
//        mem_head = null;
//        hash_size = 0;
//        max_size = 0;
//        max_clean_size = 0;
//    }
//
//    public boolean init(int hash_size, int node_size, int max_clean_size) {
//        if (0 >= hash_size || 0 >= node_size) {
//            return false;
//        }
//        // 1. init mem resource
//        this.max_clean_size = max_clean_size;
//        this.max_size = node_size;
//        for (int i = 0; i < max_size; i++) {
//            InnerCacheNode node = new InnerCacheNode();
//            node.hash_next = mem_head;
//            mem_head = node;
//        }
//        // 2. init hash
//        this.curr_size = 0;
//        this.clean_size = 0;
//        this.dirty_size = 0;
//        this.hash_size = hash_size;
//        hash_ports = new InnerCacheNode[this.hash_size];
//        for (int i = 0; i < this.hash_size; i++) {
//            hash_ports[i] = null;
//        }
//        // 3. init lru
//        lru_head = new InnerCacheNode();
//        lru_mid = new InnerCacheNode();
//        DLIST_INIT_HEAD(lru_head);
//        DLIST_ADD(lru_mid, lru_head);
//        // 4. dirty
//        dirty_head = new InnerCacheNode();
//        dirty_head.dirty_next = dirty_head;
//        dirty_head.dirty_prev = dirty_head;
//        return true;
//    }
//
//    public boolean occupy(Number key) {
//        // occupy 新加节点，加入LRU脏链，否则不对LRU链作任何操作
//        InnerCacheNode node = null;
//        synchronized (cache_mutex) {
//            node = hashFind(key);
//            if (null == node) {
//                node = malloc();
//                if (null == node) {
//                    return false;
//                }
//                node.key = key;
//                hashAdd(node);
//                lruSortDirty(node);
//                this.dirty_size ++;
//            }
//            return true;
//        }
//    }
//
//    public Object query(Number key) {
//        // 如果节点是clean的，则放在lru干净链的头部，否则，不作lru排序
//        InnerCacheNode node = null;
//        synchronized (cache_mutex) {
//            node = hashFind(key);
//            if (null != node) {
//                if (null != node.data) {
//                    if (node.dirty == false) {
//                        lruSortClean(node);
//                    }
//                    return node.data;
//                }
//            }
//            return null;
//        }
//    }
//    
//    public List<Object> getAllData() {
//        synchronized (cache_mutex) {
//            List<Object> datalist = MiscUtil.newArrayList();
//            for (int i = 0; i < hash_ports.length; ++i) {
//                InnerCacheNode hashNode = hash_ports[i];
//                InnerCacheNode currNode = hashNode;
//                while (currNode != null) {
//                    datalist.add(currNode.data);
//                    currNode = currNode.hash_next;
//                }
//            }
//            return datalist;
//        }
//    }
//
//    public Object setData(Number key, Object data) {
//        synchronized (cache_mutex) {
//            InnerCacheNode node = hashFind(key);
//            if (null == node) {
//                return null;
//            }
//            // 1. buf already exist ?
//            if (null != node.data) {
//                return node.data;
//            }
//            // 2. set the buf and put it to LRU
//            if (data != null) {
//                // 不为空，表示加载到数据, 加入LRU干净链
//                node.data = data;
//                lruSortClean(node);
//                this.dirty_size --;
//                this.clean_size ++;
//                return data;
//            } else {
//                // 查询没有数据, 回收节点
//                hashDel(key);
//                DLIST_DEL(node);
//                release(node);
//                this.dirty_size --;
//                return null;
//            }
//        }
//    }
//
//    public boolean setDirty(Number key) {
//        synchronized (cache_mutex) {
//            InnerCacheNode node = hashFind(key);
//            if (null == node) {
//                return false;
//            }
//            // 1. buf not exist ?
//            if (null == node.data) {
//                return false;
//            }
//            // 2. already dirty ?
//            if (true == node.dirty) {
//                return true;
//            }
//            // 3. set the buf and put it to LRU
//            node.dirty = true;
//            DIRTY_ADD(node, dirty_head.dirty_prev);
//            lruSortDirty(node);
//            this.dirty_size ++;
//            this.clean_size --;
//            // 4. return
//            return true;
//        }
//    }
//
//    public Object getNextDirtyNodeAndClean() {
//        synchronized (cache_mutex) {
//            InnerCacheNode node = dirty_head.dirty_next;
//            if (node == dirty_head) {
//                return null;
//            }
//            // 1. 从脏数据链删除
//            DIRTY_DEL(node);
//            node.dirty = false;
//            lruSortClean(node);
//            this.dirty_size --;
//            this.clean_size ++;
//            return node.data;
//        }
//    }
//
//    public int getCacheSize() {
//        return curr_size;
//    }
//    
//    public int getCleanSize() {
//        return clean_size;
//    }
//    
//    public int getDirtySize() {
//        return dirty_size;
//    }
//
//    // ==================================================
//    private void lruSortDirty(InnerCacheNode node) {
//        if (null == node) {
//            return;
//        }
//        // the lru sort
//        DLIST_DEL(node);
//        DLIST_CUR_NULL(node);
//        DLIST_ADD(node, lru_head.prev);
//        return;
//    }
//    
//    private void lruSortClean(InnerCacheNode node) {
//        if (null == node) {
//            return;
//        }
//        // the lru sort
//        DLIST_DEL(node);
//        DLIST_CUR_NULL(node);
//        DLIST_ADD(node, lru_mid.prev);
//        return;
//    }
//
//    //
//    // ===================================================
//    private InnerCacheNode hashFind(Number key) {
//        int base = (key.intValue() % hash_size);
//        InnerCacheNode ptr = hash_ports[base];
//        while (null != ptr) {
//            if (key.equals(ptr.key)) {
//                return ptr;
//            }
//            ptr = ptr.hash_next;
//        }
//        return null;
//    }
//
//    private boolean hashAdd(InnerCacheNode node) {
//        if (null == node) {
//            return false;
//        }
//        int base = (node.key.intValue() % hash_size);
//        // 一定不存在相同key
//        node.hash_next = hash_ports[base];
//        hash_ports[base] = node;
//        //
//        curr_size ++;
//        //
//        return true;
//    }
//
//    private boolean hashDel(Number key) {
//        int base = (key.intValue() % hash_size);
//        InnerCacheNode ptr = hash_ports[base];
//        InnerCacheNode tmp = null;
//        if (null == ptr) {
//            return false;
//        }
//        if (key.equals(ptr.key)) {
//            hash_ports[base] = hash_ports[base].hash_next;
//            ptr.hash_next = null;
//            curr_size --;
//            return true;
//        }
//
//        while (null != ptr.hash_next) {
//            if (key.equals(ptr.hash_next.key)) {
//                tmp = ptr.hash_next;
//                ptr.hash_next = tmp.hash_next;
//                tmp.hash_next = null;
//                curr_size --;
//                break;
//            }
//            ptr = ptr.hash_next;
//        }
//        return true;
//    }
//
//    // ============================================================
//    private void initCashNode(InnerCacheNode node) {
//        node.key = null;
//        node.data = null;
//        node.dirty = false;
//        node.prev = null;
//        node.next = null;
//        node.hash_next = null;
//        node.dirty_next = null;
//    }
//
//    private InnerCacheNode malloc() {
//        InnerCacheNode node = mem_head;
//        if (mem_head != null && clean_size < max_clean_size) {
//            mem_head = mem_head.hash_next;
//        }
//        else {
//            node = lru_head.next;
//            if (node != lru_mid) {
//                hashDel(node.key);
//                DLIST_DEL(node);
//                this.clean_size --;
//            }
//            else {
//                node = null;
//            }
//        }
//        //
//        if (node != null) {
//            initCashNode(node);
//        }
//        return node;
//    }
//
//    private void release(InnerCacheNode node) {
//        if (node == null) {
//            return;
//        }
//        initCashNode(node);
//        node.hash_next = mem_head;
//        mem_head = node;
//    }
//
//}
