package logic.majiong.xnStragety;

import config.JsonUtil;
import logic.majiong.PlayerDeskInfo;
import logic.majiong.XueniuFanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by Administrator on 2016/12/20.
 */
public class XnStragetyManager {
    private static XnStragetyManager ourInstance = new XnStragetyManager();

    public static XnStragetyManager getInst() {
        return ourInstance;
    }

    private static final Logger logger = LoggerFactory.getLogger(XnStragetyManager.class);

    private XnStragetyManager() {
    }

    private IXueniuHandler[] handlerList = new IXueniuHandler[XueniuFanType.END.getId()];


    public void registerAllStragety() {
        registerOneStragety(XueniuFanType.PINGHU, XueniuHandler::handlerPinghu);
        registerOneStragety(XueniuFanType.DUIDUI, XueniuHandler::handlerDuidui);
        registerOneStragety(XueniuFanType.QIDUI, XueniuHandler::handlerQidui);
        registerOneStragety(XueniuFanType.QINGYISE, XueniuHandler::handlerQingyise);
        registerOneStragety(XueniuFanType.JINGOU, XueniuHandler::handlerJingou);
        registerOneStragety(XueniuFanType.DAIYAOJIU, XueniuHandler::handlerDai19);
        registerOneStragety(XueniuFanType.QINGDUI, XueniuHandler::handlerQingduidui);
        registerOneStragety(XueniuFanType.LONGQIDUI, XueniuHandler::handlerLongqidui);
        registerOneStragety(XueniuFanType.QINGQIDUI, XueniuHandler::handlerQingQidui);
        registerOneStragety(XueniuFanType.QING19, XueniuHandler::handlerQing19);
        registerOneStragety(XueniuFanType.JIANGJINGOU, XueniuHandler::handlerJiangjingou);
        registerOneStragety(XueniuFanType.JIANGJINGGOU2, XueniuHandler::handlerJiangjingou2);
        registerOneStragety(XueniuFanType.QINGLONGQIDUI, XueniuHandler::handlerQinglongqidui);
        registerOneStragety(XueniuFanType.SHIBALUOHAN, XueniuHandler::handler18luohan);
        registerOneStragety(XueniuFanType.QINGSHIBALUOHAN, XueniuHandler::handlerQing18luohan);
        registerOneStragety(XueniuFanType.DUAN_19, XueniuHandler::handlerDuan19);
        registerOneStragety(XueniuFanType.JIANG_DUI, XueniuHandler::handlerJiangdui);
        registerOneStragety(XueniuFanType.MENQIANQING, XueniuHandler::handlerMenqianqing);
    }

    public XueniuFanType calFinalFanType(boolean isTing, PlayerDeskInfo info, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain, List<XueniuFanType> extraTypes) {
        List<XueniuFanType> fanList = new ArrayList<>();
        int length = handlerList.length;
        for (int i = 0; i < length; i++) {
            if (handlerList[i] == null) {
                continue;
            }
            XueniuFanType type = XueniuFanType.getByValue(i);
            if (type == null) {
                continue;
            }
            if (type.isExtra() && !extraTypes.contains(type)) {
                continue;
            }
            if (handlerList[i].handler(fanList, gang, ke, chi, remain)) {
                fanList.add(XueniuFanType.getByValue(i));
            }
        }

        // 計算天地胡
        if (!isTing) { // 听牌不計算天胡
            XueniuFanType tianDiHu = handleTianDiHu(info);
            if (tianDiHu != null) {
                if (extraTypes.contains(tianDiHu)) {
                    fanList.add(tianDiHu);
                }
            }
        }

        logger.info("{}|{} 玩家{} 算翻 {}", info.getDesk().getDeskId(), info.getDesk().getGameId(),
                info.getDesk().getPlayerByPosition(info.getPosition()).getPlayerId(), JsonUtil.getJsonString(fanList));
        return choseTheBiggestFan(fanList);
    }


    // 計算天地胡
    public static XueniuFanType handleTianDiHu(PlayerDeskInfo info) {
        if (info == null) {
            return null;
        }
        if (info.isZhuang()) {
            if (info.neverHandCard() && info.getTotalCardCotainGangChike() == 14) {
                return XueniuFanType.TIANHU;
            }
        } else {
            if (info.neverHandCard()) {
                return XueniuFanType.DIHU;
            }
            if (info.getAddHandTimes() == 1 && info.getTotalCardCotainGangChike() == 14) {
                return XueniuFanType.DIHU;
            }
        }

        return null;
    }

    public XueniuFanType choseTheBiggestFan(List<XueniuFanType> typeList) {
        if (typeList == null) {
            return null;
        }
        XueniuFanType result = null;
        for (XueniuFanType type : typeList) {
            if (result == null || result.getFan() < type.getFan()) {
                result = type;
            }
        }
        return result;
    }


    public void registerOneStragety(XueniuFanType type, IXueniuHandler handler) {
        handlerList[type.getId()] = handler;
    }
}
