//package db.data;
//
//
//public enum DataStat {
//    VALID(1),        // 正常，
//    NEW(2),          // 新数据，需要刷新到数据库 
//    DELETE(3),       // 删除数据，需要从数据库删除此数据
//    ;
//
//    private final int i;
//
//    private DataStat(int i) {
//        this.i = i;
//    }
//
//    public int getValue() {
//        return i;
//    }
//
//    public static DataStat getByValue(int value) {
//        for (DataStat stat : DataStat.values()) {
//            if (stat.getValue() == value) {
//                return stat;
//            }
//        }
//        return null;
//    }
//}
