//package db;
//
//import actor.CenterActorManager;
//import actor.IActor;
//import actor.IRunner;
//import db.cache.DbCache;
//import db.cache.MulSearch;
//import db.cache.MulSearch.LinkNode;
//import db.data.DBAction;
//import db.data.ILoadResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import util.ASObject;
//
//import java.util.List;
//import java.util.concurrent.ScheduledFuture;
//
//public class DataManager {
//	private static final Logger logger = LoggerFactory.getLogger(DataManager.class);
//	private static DataManager instance = new DataManager();
//
//	public static int SAVE_CHAR_PER_SEC = 200;
//
//	private DataManager() {
//	}
//
//	public static DataManager getInst() {
//		return instance;
//	}
//
//	@SuppressWarnings("unused")
//	private boolean ready = false;
//
//	private DbCache dataCache = null;
//
//	private MulSearch<ILoadResult> queryCache = null;
//
//	private ScheduledFuture<?> checkTimer = null;
//
//	public boolean init(int max_num, int clean_size) {
//		// 1. init data cache
//		dataCache = new DbCache();
//		int node_size = max_num + 100;
//		int hash_size = node_size * 3 / 4;
//		if (false == dataCache.init(hash_size, node_size, clean_size)) {
//			return false;
//		}
//		// 2. init query cache
//		queryCache = new MulSearch<ILoadResult>();
//		if (false == queryCache.init(hash_size)) {
//			return false;
//		}
//		// 3. ready
//		// OK!
//		ready = true;
//		return true;
//	}
//
//	public boolean loadAllData() {
//		return true;
//	}
//
//
//	public boolean start() {
//		checkTimer = CenterActorManager.getDBTimer().register(1 * 1000L, 1 * 1000L, new Runnable() {
//			@Override
//			public void run() {
//				for (int i = 0; i < SAVE_CHAR_PER_SEC; i++) {
//					Object data = dataCache.getNextDirtyNodeAndClean();
//					if (data == null) {
//						break;
//					}
//					final CharData charData = (CharData) data;
//					int charId = charData.getCharId();
//					logger.info("Saving char====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>:[" + charId + "] TO DB");
//					IActor actor = CenterActorManager.getDbActor(charId);
//					actor.put(() -> {
//						try {
//							synchronized (charData) {
//								charData.saveAll();
//							}
//						} catch (Exception exception) {
//							logger.error("", exception);
//						}
//						return null;
//					});
//				}
//			}
//		}, CenterActorManager.getDbCheckActor(), "DB_CHECK");
//		return true;
//	}
//
//	public void stop() {
//		if (checkTimer != null) {
//			checkTimer.cancel(true);
//		}
//		checkTimer = null;
//	}
//
//	public void flush() {
//		List<Object> dataList = dataCache.getAllData();
//		logger.info("FLUSH BEGIN with total size = " + dataList.size());
//		int flushCnt = 0;
//		for (Object data : dataList) {
//			if (data == null) {
//				logger.info("FLUSH ING with null data, continue");
//				continue;
//			}
//			CharData charData = (CharData) data;
//			try {
//				synchronized (charData) {
//					charData.saveAll();
//					flushCnt++;
//				}
//			} catch (Exception e) {
//				logger.error("", e);
//			}
//			logger.info("FLUSH ING with the " + flushCnt + " char data finished");
//		}
//		logger.info("FLUSH END ");
//	}
//
//	public void saveModule(int playerId, DBAction action, ASObject data) {
//		CharData charData = (CharData) dataCache.query(playerId);
//		if (charData == null) {
//			System.out.print("the player is null in cache  why ");
//			return;
//		}
//		charData.updateModuleData(action, data);
//		dataCache.setDirty(playerId);
//	}
//
//
//	public void loadChar(final int charId, ILoadResult callBack) {
//		synchronized (queryCache) {
//			LinkNode<ILoadResult> iterator = queryCache.Search(charId);
//			if (false == queryCache.AddKey(charId, callBack)) {
//				if (callBack != null) {
//					callBack.onResult(null, "queryCache.AddKey" + charId);
//				}
//				logger.info("Char:" + charId + "queryCache.AddKey fail, cache size = "
//						+ dataCache.getCacheSize() + " clean_size= " + dataCache.getCleanSize() + " dirty_size= "
//						+ dataCache.getDirtySize());
//				return;
//			}
//			if (iterator == null) {
//				if (false == dataCache.occupy(charId)) {
//					queryCache.DeleteKey(charId);
//					if (callBack != null) {
//						callBack.onResult(null, "dataCache.occupy" + charId);
//					}
//					logger.info("Char:" + charId + "dataCache.occupy fail, cache size = "
//							+ dataCache.getCacheSize() + " clean_size= " + dataCache.getCleanSize()
//							+ " dirty_size= " + dataCache.getDirtySize());
//					return;
//				}
//				CenterActorManager.getLoadActor(charId).put(new IRunner() {
//					@Override
//					public Object run() {
//						CharData charData = new CharData();
//						charData.init(charId);
//						boolean ret = false;
//						try {
//							ret = charData.loadAll();
//						} catch (Exception e) {
//							logger.error("", e);
//							ret = false;
//						}
//						if (ret) {
//							charData = (CharData) dataCache.setData(charId, charData);
//							logger.info("Char:" + charId + "loaded, cache size = " + dataCache.getCacheSize()
//									+ " clean_size= " + dataCache.getCleanSize() + " dirty_size= "
//									+ dataCache.getDirtySize());
//						} else {
//							charData = (CharData) dataCache.setData(charId, null);
//							logger.info("Char:" + charId + "load fail, cache size = "
//									+ dataCache.getCacheSize() + " clean_size= " + dataCache.getCleanSize()
//									+ " dirty_size= " + dataCache.getDirtySize());
//						}
//						synchronized (queryCache) {
//							LinkNode<ILoadResult> iterator = queryCache.Search(charId);
//							while (iterator != null) {
//								ILoadResult callback = iterator.data;
//								if (callback != null) {
//									callback.onResult(charData, "" + charId);
//								}
//								iterator = iterator.same_next;
//							}
//							queryCache.DeleteKey(charId);
//						}
//						return null;
//					}
//				});
//			} else {
//				// do nothing, 已经加入链了
//			}
//		}
//	}
//
//	public DbCache getCache() {
//		return dataCache;
//	}
//}
