package config;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.bean.ConfMatchCard;
import config.bean.ConfMatchCardInfo;
import config.bean.CoupleRoom;
import config.bean.GrabNiuConfig;
import config.bean.RoomConfig;
import config.provider.BaseProvider;
import database.DBUtil;
import database.DataQueryResult;
import logic.define.GameType;
import logic.poker.PokerCard;
import logic.poker.PokerUtil;
import util.ASObject;

public class CoupleRoomInfoProvider extends BaseProvider {
	private Logger logger = LoggerFactory.getLogger(CoupleRoomInfoProvider.class);
	
	private static CoupleRoomInfoProvider ourInstance = new CoupleRoomInfoProvider();

	public static CoupleRoomInfoProvider getInst() {
		return ourInstance;
	}

	static {
		BaseProvider.providerList.add(ourInstance);
	}

	private Map<Integer, CoupleRoom> coupleRoomCfgMap = new HashMap<>();
	private Map<Integer, RoomConfig> conf_room_ex = new HashMap<>();
	private Map<Integer, GrabNiuConfig> confGrabNiu = null;
	private Map<Integer, ConfMatchCard> confMatchCardMap;

	private CoupleRoomInfoProvider() {
	}

	public void doLoad() {
		loadConfRoom();
		loadConfRoomEx();
		loadConfGrabNiu();
		loadConfMatchCard();
	}

	public void loadConfRoom() {
		Map<Integer, CoupleRoom> coupleRoomCfgMap = new HashMap<>();
		List<ASObject> roomList = DataQueryResult.load("conf_room", null);
		roomList.forEach(e -> coupleRoomCfgMap.put(e.getInt("id"), new CoupleRoom(e)));
		this.coupleRoomCfgMap = coupleRoomCfgMap;
	}

	public void loadConfRoomEx() {
		Map<Integer, RoomConfig> conf_room_ex = new HashMap<>();
		List<ASObject> roomList = DataQueryResult.load("conf_room_ex", null);
		roomList.forEach(e -> conf_room_ex.put(e.getInt("id"), new RoomConfig(e)));
		this.conf_room_ex = conf_room_ex;
	}

	public void loadConfGrabNiu() {
		Map<Integer, GrabNiuConfig> conf_grab_niu = new HashMap<>();
		List<ASObject> grab_niu_List = DataQueryResult.load("conf_grab_niu", null);
		grab_niu_List.forEach(e -> conf_grab_niu.put(e.getInt("room_id"), new GrabNiuConfig(e)));
		this.confGrabNiu = conf_grab_niu;
	}
	
	public void loadConfMatchCard(){
		List<ASObject> listDdzCard = DataQueryResult.load("conf_match_card_info", null);
		Map<Integer, ConfMatchCardInfo> ddzCard = new HashMap<>();
		for (ASObject obj : listDdzCard) {
			int id = obj.getInt("id");
			String desc = obj.getString("desc");
			int rand_group_num = obj.getInt("rand_group_num");
			GameType game = GameType.getByValue(obj.getInt("game_type"));
			ConfMatchCardInfo confMatchCardDdz =  new ConfMatchCardInfo(id,game,desc,rand_group_num);
			confMatchCardDdz.loadDdzCards(obj);
			//判断合法性
			Queue<PokerCard> mixedCard = PokerUtil.mixAllCard(game);
			List<PokerCard> cards = confMatchCardDdz.getCards();
			if( cards.size() > mixedCard.size()){
				logger.error("斗地主配牌牌型数量错误!,不使用配牌{} {} 数量{}", id,desc,cards.size());
				continue;
			}
			boolean cardError = false;
			Set<PokerCard> hasCard = new HashSet<>();
			for (PokerCard card :cards) {
				if(!mixedCard.contains(card)){
					logger.error("斗地主配牌牌型不存在!,不使用配牌{} {} 数量{}", id,desc,card);
					cardError = true;
					break;
				}
				if(hasCard.contains(card)){
					logger.error("斗地主配牌牌型重复!,不使用配牌{} {} 数量{}", id,desc,card);
					cardError = true;
					break;
				}
				hasCard.add(card);
			}
			if(cardError){
				continue;
			}
			ddzCard.put(id, confMatchCardDdz);
		}
		
		List<ASObject> list = DataQueryResult.load("conf_match_card", null);
		
		Map<Integer, ConfMatchCard> confMatchCardMap = new HashMap<>();
		for (ASObject obj : list) {
			int roomId = obj.getInt("room_id");
			int status = obj.getInt("status");
			int rate = obj.getInt("rate");
			ConfMatchCard confMatchCard =  new ConfMatchCard(status,rate);
			String match_niu = obj.getString("match_niu");
			String match_grab_niu = obj.getString("match_grab_niu");
			String zjh_players = obj.getString("zjh_players");
			String zjh_card = obj.getString("zjh_card");
			String ddz_card = obj.getString("ddz_card");
			
			if(match_niu != null && !match_niu.trim().equals("")){
				confMatchCard.loadConfMatchNiuCard(match_niu);
			}
			
			if(match_grab_niu != null && !match_grab_niu.trim().equals("")){
				confMatchCard.loadConfMatchGrabNiuCard(match_grab_niu);
			}
			
			if(zjh_players != null && !zjh_players.trim().equals("")){
				confMatchCard.loadZjhPlayers(zjh_players);
			}
			
			if(zjh_card != null && !zjh_card.trim().equals("")){
				confMatchCard.loadZjhCard(zjh_card);
			}
			if(ddz_card != null && !ddz_card.trim().equals("")){
				confMatchCard.loadDdzCard(ddzCard,ddz_card);
			}
			
			confMatchCardMap.put(roomId, confMatchCard);
		}
		
		this.confMatchCardMap = confMatchCardMap;
	}

	private void saveToDataBase(CoupleRoom conf) {
		Map<String, Object> data = new HashMap<>();
		data.put("id", conf.getId());
		data.put("lowScore", getBaseScoreOfRoom(conf.getId()));
		data.put("startValue", conf.getMinReq());
		data.put("endValue", conf.getMaxReq());
		data.put("descript", conf.getLimitStr());
		data.put("mode", conf.getMode());
		data.put("order1", conf.getOrder());
		data.put("btnBg", conf.getBtnBg());
		data.put("classify", conf.getClassify());
		data.put("coin_icon", conf.getCoinIcon());
		data.put("people_icon", conf.getPeopleIcon());
		data.put("tax_rate", conf.getTax_rate());
		try {
			DBUtil.executeInsert("conf_room", data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public Map<Integer, CoupleRoom> getCoupleRoomCfgMap(GameType game) {
		Map<Integer, CoupleRoom> result = new HashMap<>();
		for (CoupleRoom room : coupleRoomCfgMap.values()) {
			if (room.getMode() == game.getValue()) {
				result.put(room.getId(), room);
			}
		}
		return result;
	}

	@Override
	protected void initString() {
		confString = JsonUtil.getGson().toJson(coupleRoomCfgMap, Map.class);
	}

	public CoupleRoom getRoomConf(int id) {
		return coupleRoomCfgMap.get(id);
	}

	public GrabNiuConfig getGrabNiuConf(int roomId) {
		return confGrabNiu.get(roomId);
	}

	public int getEnterLimit(int id) {
		CoupleRoom conf = getRoomConf(id);
		if (conf == null) {
			return 99999999;
		}
		return conf.getMinReq();
	}

	public int getBaseScoreOfRoom(int roomId) {
		CoupleRoom conf = coupleRoomCfgMap.get(roomId);
		return conf == null ? 0 : (int) (conf.getBase() * 100);
	}

	public int getFastRoomId(long coin) {
		CoupleRoom result = null;
		long minValue = Long.MIN_VALUE;
		for (CoupleRoom conf : coupleRoomCfgMap.values()) {
			if (coin - conf.getMinReq() < 0) {
				continue;
			}
			if (result == null || (coin - conf.getMinReq()) < minValue) {
				minValue = coin - conf.getMinReq();
				result = conf;
			}
		}
		return result == null ? -1 : result.getId();
	}

	public int getTaxRate(int id) {
		if(null == coupleRoomCfgMap.get(id)) {
			/// 给一个默认值
			return 1;
		}
		return coupleRoomCfgMap.get(id).getTax_rate();
	}

//	public int getGameTypeToTaxRate(int type) {
//		for(Map.Entry<Integer, CoupleRoom> map_data : coupleRoomCfgMap.entrySet()) {
//			if(map_data.getValue().getMode() == type) {
//				return map_data.getValue().getTax_rate();
//			}
//		}
//		/// 给一个默认值
//		return 1;
//	}

	public RoomConfig getConfRoomEx(int room_id) {
		for(Map.Entry<Integer, RoomConfig> entry : conf_room_ex.entrySet()) {
			if(entry.getValue().getRoom_id() == room_id) {
				return  entry.getValue();
			}
		}

		return null;
	}
	
	public ConfMatchCard getConfMatchCard(int roomId){
		return confMatchCardMap.get(roomId);
	}
}
