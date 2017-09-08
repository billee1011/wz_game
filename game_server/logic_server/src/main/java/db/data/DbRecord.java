package db.data;

import util.MapObject;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

public class DbRecord {

	public long key = 0;
	public DataStat stat = DataStat.VALID;
	public MapObject data = null;

	public DbRecord(MapObject data) {
		MapObject target = new MapObject();
		for (Entry<String, Object> entry : data.entrySet()) {
			String keyS = entry.getKey();
			if (entry.getValue() != null && entry.getValue() instanceof byte[]) {
				byte[] buffer = (byte[]) entry.getValue();
				target.put(keyS, buffer);
			} else {
				target.put(keyS, entry.getValue());
			}
		}
		//
		this.data = target;
		this.key = DataUtil.crc32(this.data);
		this.stat = DataStat.VALID;
	}

	public static boolean sameRecord(MapObject data1, MapObject data2, List<String> keys) {
		for (String key : keys) {
			long l1 = data1.getLong(key, -1);
			long l2 = data2.getLong(key, -1);
			if (l1 == -1 && l2 == -1) {
				Object v1 = data1.get(key);
				Object v2 = data2.get(key);
				if (v1.equals(v2) == false) {
					return false;
				}
			} else {
				if (l1 != l2) {
					return false;
				}
			}
			/*
            Object v1 = data1.get(key);
            Object v2 = data2.get(key);
            if (v1 instanceof Number && v2 instanceof Number) {
                long l1 = ((Number) v1).longValue();
                long l2 = ((Number) v2).longValue();
                if (l1 != l2) {
                    return false;
                }
            }
            else if (v1.equals(v2) == false) {
                return false;
            }
            */
		}
		return true;
	}

	public static void update(List<DbRecord> records, Object[] moduleDatas, List<String> keys,
							  boolean desableDelete) {
		if (records == null || moduleDatas == null) {
			return;
		}
		List<DbRecord> moduleRecords = MiscUtil.newArrayList();
		for (Object data : moduleDatas) {
			DbRecord record = new DbRecord((MapObject) data);
			moduleRecords.add(record);
		}
		// 1. get all in moduleData not in records
		List<DbRecord> moduleOnlys = MiscUtil.newArrayList();
		for (DbRecord record : moduleRecords) {
			boolean match = false;
			for (DbRecord inirecord : records) {
				if (DbRecord.sameRecord(record.data, inirecord.data, keys)) {
					if (inirecord.key != record.key) {
						inirecord.data = record.data;
						inirecord.stat = DataStat.NEW;
						inirecord.key = record.key;
					}
					match = true;
					break;
				}
			}
			if (match == false) {
				moduleOnlys.add(record);
			}
		}
		// 2. get all in records not in moduleData, set delete
		if (desableDelete == false) {
			for (DbRecord inirecord : records) {
				boolean match = false;
				for (DbRecord record : moduleRecords) {
					if (DbRecord.sameRecord(record.data, inirecord.data, keys)) {
						match = true;
						break;
					}
				}
				if (match == false) {
					inirecord.stat = DataStat.DELETE;
				}
			}
		}
		// 3. add all in moduleData not in records
		for (DbRecord record : moduleOnlys) {
			record.stat = DataStat.NEW;
			records.add(record);
		}
	}

	public static boolean checkSameKeySet(List<DbRecord> records, Object[] moduleDatas, List<String> keys) {
		if (records == null || moduleDatas == null) {
			return true;
		}
		List<DbRecord> moduleRecords = MiscUtil.newArrayList();
		for (Object data : moduleDatas) {
			DbRecord record = new DbRecord((MapObject) data);
			moduleRecords.add(record);
		}
		// 1. get all in moduleData not in records
		for (DbRecord record : moduleRecords) {
			boolean match = false;
			for (DbRecord inirecord : records) {
				if (DbRecord.sameRecord(record.data, inirecord.data, keys)) {
					if (inirecord.key != record.key) {
						inirecord.data = record.data;
						inirecord.stat = DataStat.NEW;
						inirecord.key = record.key;
					}
					match = true;
					break;
				}
			}
			if (match == false) {
				return false;
			}
		}
		return true;
	}

	public static void save(List<DbRecord> records, String table, List<String> keys) throws SQLException {
		if (records == null || records.size() == 0) {
			return;
		}
		// 1. updates
		List<MapObject> updates = MiscUtil.newArrayList();
		for (DbRecord record : records) {
			if (record.stat == DataStat.NEW) {
				updates.add(record.data);
			}
		}
		if (updates.size() > 0) {
			Object[] updatelist = updates.toArray();
//            DBUtil.batchInsertOrUpdate(table, keys, updatelist);
		}
		// 2. deletes
		List<DbRecord> toDelete = MiscUtil.newArrayList();
		List<MapObject> deletes = MiscUtil.newArrayList();
		for (DbRecord record : records) {
			if (record.stat == DataStat.DELETE) {
				deletes.add(record.data);
				toDelete.add(record);
			}
		}
		if (deletes.size() > 0) {
			Object[] deletelist = deletes.toArray();
//            DBUtil.batchDelete(table, keys, null, deletelist);
		}
		for (DbRecord record : toDelete) {
			records.remove(record);
		}
		for (DbRecord record : records) {
			record.stat = DataStat.VALID;
		}
	}
}
