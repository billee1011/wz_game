package protobuf;

import chr.RyCharacter;
import proto.Login;

/**
 * Created by think on 2017/9/8.
 */
public class LoginPbCreator {

	//创建登陆成功的PB

	public static Login.PBLoginSucc loginSucc(RyCharacter ch) {
		Login.PBLoginSucc.Builder builder = Login.PBLoginSucc.newBuilder();
		builder.setPlayerId(ch.getEntityId());
		builder.setUserId(ch.getUserId());
		builder.setName(ch.getPlayerName());
		builder.setBattleScore(ch.getCharFormation().calBattleScore());
		builder.setTili(ch.getTili());
		builder.setJingli(ch.getJingli());
		ch.getResourceManager().getResourceMap().forEach((e, f) -> builder.putResMap(e.getValue(), f));
		return builder.build();
	}
}
