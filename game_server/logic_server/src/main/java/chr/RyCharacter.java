package chr;

import actor.LogicActorManager;
import base.EntityCreator;
import base.IEntity;
import chr.equip.CharEquip;
import chr.equip.EquipEntity;
import chr.fotmation.CharFormation;
import chr.hero.CharHero;
import chr.hero.HeroEntity;
import chr.resource.ResourceManager;
import com.google.protobuf.MessageLite;
import define.Constant;
import define.EMoney;
import define.EntityType;
import io.netty.channel.ChannelHandlerContext;
import packet.CocoPacket;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import util.LogUtil;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by think on 2017/9/7.
 */
public class RyCharacter extends IEntity {

	private int userId;

	private String playerName;

	private ResourceManager resourceManager;

	private CharHero charHero;

	private CharEquip charEquip;

	private CharFormation charFormation;

	private int tili;

	private int jingli;

	private int tiliRemain;

	private int jingliRemain;

	private long lastSaveTime;

	private ChannelHandlerContext ioSession;

	private RyCharacter() {
		super(EntityType.CHARACTER);
		charHero = new CharHero(this);
		resourceManager = new ResourceManager(this);
		charFormation = new CharFormation(this);
		charEquip = new CharEquip(this);
		lastSaveTime = System.currentTimeMillis();
	}

	private ScheduledFuture<?> recoveryFuture;

	public void setIoSession(ChannelHandlerContext ctx) {
		this.ioSession = ctx;
	}

	//恢复需要恢复的所有资源
	public void beginRecovery() {
		recoveryFuture = LogicActorManager.getTimer().register(1000, 1000, this::update, LogicActorManager.getLogicActor(), "recovery");
	}

	public void endRecovery() {
		if (recoveryFuture != null) {
			recoveryFuture.cancel(true);
			recoveryFuture = null;
		}
	}


	void update() {
		updateJingli();
		updateTili();
	}

	void updateJingli() {
		if (isJingliFull())
			return;
		jingliRemain--;
		if (jingliRemain == 0) {
			this.jingli++;
			jingliRemain = Constant.JINGLI_NEED_TIME;
		}
	}

	void updateTili() {
		if (isTiliFull())
			return;
		tiliRemain--;
		if (tiliRemain == 0) {
			this.tili++;
			tiliRemain = Constant.TILI_NEED_TIME;
		}
	}

	public CharEquip getCharEquip() {
		return charEquip;
	}

	public boolean isTiliFull() {
		return true;
	}

	public boolean isJingliFull() {
		return true;
	}

	public int getTili() {
		return tili;
	}

	public int getJingli() {
		return jingli;
	}

	public void setTili(int tili) {
		this.tili = tili;
	}

	public void setJingli(int jingli) {
		this.jingli = jingli;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public CharHero getCharHero() {
		return charHero;
	}

	public CharFormation getCharFormation() {
		return charFormation;
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}


	//根据性别找个主将
	public static RyCharacter getDefault(long entityId, String name, int userId) {
		RyCharacter character = new RyCharacter();
		character.setEntityId(entityId);
		character.setPlayerName(name);
		character.setUserId(userId);
		character.setTili(Constant.TILI_INIT_COUNT);
		character.setJingli(Constant.JINGLI_INIT_COUNT);
		//初始化一个英雄给玩家
		HeroEntity heroEntity = EntityCreator.createHero(Constant.MAIN_HERO_ID);                    //创建出来之后首先添加到英雄列表里面
		character.getCharHero().addEntity(heroEntity);                                                  //加入以后就要让英雄上阵了
		character.getResourceManager().updateResource(EMoney.DIAMOND, 50, true);                      //初始化给玩家增加50元宝1000银币
		character.getResourceManager().updateResource(EMoney.SILVER, 10000, true);
		character.getCharFormation().addHeroIntoFormation(heroEntity);
		character.getCharEquip().addEntity(EntityCreator.createEquipEntity(200001));
		character.getCharEquip().addEntity(EntityCreator.createEquipEntity(200002));
		character.getCharEquip().addEntity(EntityCreator.createEquipEntity(200003));
		character.getCharEquip().addEntity(EntityCreator.createEquipEntity(200004));
		return character;
	}

	public static RyCharacter getEmptyChar(long playerId) {
		RyCharacter ch = new RyCharacter();
		ch.setEntityId(playerId);
		return ch;
	}

	public void write(ResponseCode code, MessageLite message) {
		if (message != null) {
			LogUtil.msgLogger.info("player id is {} write message {} and length is {} and the content is {}", getEntityId(), code, message.toByteArray().length, message);
		} else {
			LogUtil.msgLogger.info("player id is {} write message {} and length is {} and the content is {}", getEntityId(), code);
		}
		write(new CocoPacket(code.getValue(), message == null ? null : message.toByteArray(), getEntityId()));
	}

	public void write(RequestCode code, MessageLite message) {
		if (message != null) {
			LogUtil.msgLogger.info("player id is {} write message {} and length is {} and the content is {}", getEntityId(), code, message.toByteArray().length, message);
		} else {
			LogUtil.msgLogger.info("player id is {} write message {} and length is {} and the content is {}", getEntityId(), code);
		}
		write(new CocoPacket(code.getValue(), message == null ? null : message.toByteArray(), getEntityId()));
	}

	public void write(CocoPacket packet) {
		ChannelHandlerContext session = this.ioSession;
		if (session == null) {
			return;
		}
		packet.resetReqIdToRequest();
		if (session != null) {
			session.writeAndFlush(packet);
		}
	}
}
