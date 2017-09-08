package chr;

import base.EntityCreator;
import base.IEntity;
import chr.hero.CharHero;
import chr.hero.HeroEntity;
import chr.resource.ResourceManager;
import com.google.protobuf.MessageLite;
import define.Constant;
import define.EMoney;
import define.EntityType;
import io.netty.channel.ChannelHandlerContext;
import packet.CocoPacket;
import protocol.s2c.ResponseCode;
import util.LogUtil;

/**
 * Created by think on 2017/9/7.
 */
public class RyCharacter extends IEntity {

    private int userId;

    private String playerName;

    private CharHero charHero;

    private ResourceManager resourceManager;

    private ChannelHandlerContext ioSession;

    private RyCharacter() {
        super(EntityType.CHARACTER);
        charHero = new CharHero(this);
        resourceManager = new ResourceManager(this);
    }

    public void setIoSession(ChannelHandlerContext ctx) {
        this.ioSession = ctx;
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

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    //根据性别找个主将
    public static RyCharacter getDefault(long entityId, String name, int userId) {
        RyCharacter character = new RyCharacter();
        character.setEntityId(entityId);
        character.setPlayerName(name);
        character.setUserId(userId);
        //初始化一个英雄给玩家
        HeroEntity heroEntity = EntityCreator.createHero(Constant.MAIN_HERO_ID);                    //创建出来之后首先添加到英雄列表里面
        character.getCharHero().addHero(heroEntity);                                                  //加入以后就要让英雄上阵了
        character.getResourceManager().updateResource(EMoney.DIAMOND, 50, true);                      //初始化给玩家增加50元宝1000银币
        character.getResourceManager().updateResource(EMoney.SILVER, 10000, true);
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
