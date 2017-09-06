package protobuf.creator;

import java.util.List;
import java.util.Map;

import chr.Player;
import protobuf.Lobby;
import protobuf.Lobby.PBGetPlayerNumRes;
import rank.RankItem;
import util.ASObject;
import util.Pair;

public class LobbyCreator {

	public static Lobby.PBGameRoomInfoRes createPBGameRoomInfoRes(List<Pair<Integer, Integer>> rommNumList) {
		Lobby.PBGameRoomInfoRes.Builder builder = Lobby.PBGameRoomInfoRes.newBuilder();
		rommNumList.forEach(e -> builder.addRoomPlayerList(CommonCreator.createPBPair(e.getLeft(), e.getRight())));
		return builder.build();
	}

	public static Lobby.PBRankList createPBRankList(Player player, List<RankItem> list) {
		Lobby.PBRankList.Builder builder = Lobby.PBRankList.newBuilder();
		builder.setSelfSocre(player.getScore());
		list.forEach(e -> builder.addItem(createPBRankItem(e)));
		return builder.build();
	}

	public static Lobby.PBPaomadeng createPBPaomadeng(int id, int timeFrom, int timeTo, String content, int delay) {
		Lobby.PBPaomadeng.Builder builder = Lobby.PBPaomadeng.newBuilder();
		builder.setId(id);
		builder.setStartTime(timeFrom);
		builder.setEndTime(timeTo);
		builder.setContent(content);
		builder.setDelay(delay);
		return builder.build();
	}

	public static Lobby.PBPaomadengList createPBPaomadengList(List<ASObject> list_object, int type) {
		Lobby.PBPaomadengList.Builder builder = Lobby.PBPaomadengList.newBuilder();
		builder.setOpType(type);
		list_object.forEach(e -> {
			builder.addPaoMaDengs(createPBPaomadeng(e.getInt("id"), e.getInt("timeFrom"), e.getInt("timeTo"), e.getString("content"), e.getInt("delay")));
		});
		return builder.build();
	}

	public static Lobby.PBRankItem createPBRankItem(RankItem item) {
		Lobby.PBRankItem.Builder builder = Lobby.PBRankItem.newBuilder();
		builder.setPlayerId(item.getId());
		builder.setSocre(item.getScore());
		builder.setAccount(item.getAccount());
		builder.setRank(item.getRank());
		return builder.build();
	}
	
	
	public static Lobby.PBGetPlayerNumRes createPBGetPlayerNumRes(int module,Map<Integer, Integer> map) {
		PBGetPlayerNumRes.Builder builder = PBGetPlayerNumRes.newBuilder();
		builder.setModule(module);
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			builder.addList(CommonCreator.createPBPair(entry.getKey(), entry.getValue()));
		}
		return builder.build();
	}
	

}
