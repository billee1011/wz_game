package logic.poker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.bean.ConfMatchCard;
import config.bean.ConfMatchCardInfo;
import logic.poker.niuniu.NiuResult;
import util.Pair;
import util.Randomizer;

public class PokerMatchCardUtil {
	private static  Logger logger = LoggerFactory.getLogger(PokerMatchCardUtil.class);
	
	/** 配置斗地主牌型 */
	public static Queue<PokerCard> matchDdzCard(long gameId,Queue<PokerCard> mixedCard,ConfMatchCard confMatchCard,int joinPlayerCount){
		ConfMatchCardInfo confMatchCardDdz = confMatchCard.getDdzCards();
		List<List<PokerCard>> cardGroup = confMatchCardDdz.getCardList();
		if(cardGroup != null){
			List<Integer> noFullPosList = new ArrayList<>();
			Map<Integer, Integer> cardNumMap = new HashMap<>();
			for (int i = 0; i < joinPlayerCount; i++) {
				noFullPosList.add(i);
				cardNumMap.put(i, 0);
			}
			Map<Integer, List<List<PokerCard>>> posCardMap = new HashMap<>();
			for (List<PokerCard> cards : cardGroup) {
				if(noFullPosList.size() == 0){
					break;
				}
				List<Integer> otherNoFullPosList = new ArrayList<>(noFullPosList);
				Integer pos = null;
				while(otherNoFullPosList.size() > 0){
					pos = otherNoFullPosList.get(Randomizer.nextInt(otherNoFullPosList.size()));
					Integer cardNumTotal = cardNumMap.get(pos);
					if(cardNumTotal + cards.size() > 17){
						//如果满了随其他玩家,如果都满了,其实这个牌组配置有问题
						otherNoFullPosList.remove(pos);
						pos = null;
					}else{
						cardNumTotal += cards.size();
						cardNumMap.put(pos, cardNumTotal);
						otherNoFullPosList.clear();
					}
				}
				
				if(pos == null){
					logger.error("{}斗地主牌型配置有问题,牌组id={},{}",gameId,confMatchCardDdz.getId(),confMatchCardDdz.getCardList());
					continue;
				}
				
				List<List<PokerCard>> cardsGroupList = posCardMap.get(pos);
				if(cardsGroupList == null){
					cardsGroupList = new ArrayList<>();
					posCardMap.put(pos, cardsGroupList);
				}
				cardsGroupList.add(cards);
				if(cardsGroupList.size() >= confMatchCardDdz.getRandGroupNum()){
					noFullPosList.remove(pos);
				}
				mixedCard.removeAll(cards);
			}
			List<List<PokerCard>> allHandCards = new ArrayList<>();
			for (Entry<Integer, List<List<PokerCard>>> entry : posCardMap.entrySet()) {
				List<PokerCard> handCards = new ArrayList<>();
				List<List<PokerCard>> listListCard = entry.getValue();
				for (List<PokerCard> listCard : listListCard) {
					handCards.addAll(listCard);
				}
				for (int i = handCards.size(); i < 17; i++) {
					handCards.add(mixedCard.poll());
				}
				allHandCards.add(handCards);
			}
			Queue<PokerCard> result = new LinkedList<>();
			while(allHandCards.size() > 0){
				List<PokerCard> handCards = allHandCards.remove(Randomizer.nextInt(allHandCards.size()));
				logger.info("{}最终牌型{}",gameId,handCards);
				result.addAll(handCards);
			}
			result.addAll(mixedCard);
			return result;
		}
		return mixedCard;
	}
	
	/** 配置扎金花牌型 */
	public static Queue<PokerCard> matchZjhCard(long gameId,Queue<PokerCard> mixedCard,ConfMatchCard confMatchCard,int joinPlayerCount){
		//多少人配牌多少人
		Integer needMatchPlayerNum = confMatchCard.getZjhNeedMatchNum(joinPlayerCount);
		if(needMatchPlayerNum == null){
			return mixedCard;
		}
		joinPlayerCount = needMatchPlayerNum;
//		40%	30%	20%	10%
//		A大单张	对子	顺子	金花
		Set<PokerCard> allPoker = new HashSet<>(mixedCard);
    	Queue<PokerCard> result = new LinkedList<>();
    	List<List<PokerCard>> allHandCards = new ArrayList<>();
    	PokerStore pokerStore = new PokerStore(allPoker);
		Map<Integer, Integer> indexTypeMap = new HashMap<>();
		
		for (int i = 0; i < joinPlayerCount; i++) {
    		int type = confMatchCard.getZjhCardType();
    		if(type == 0){//A大单张
    			PokerCard a = pokerStore.get1APoker();
    			if(a == null){//不够了
        			type = Randomizer.nextInt(1, 4);
    			}else{
        			logger.info("{}  A大单张  {}",gameId,a);
        			List<PokerCard> list4Card = new ArrayList<>();
        			list4Card.add(a);
        			allHandCards.add(list4Card);
        		}
    		}
    		indexTypeMap.put(i, type);
    	}
		
		for (int i = 0; i < joinPlayerCount; i++) {
    		int type = indexTypeMap.get(i);
    		if(type == -1 || type == 0){
    			if(type == -1){
    				logger.error("{}  随机种类异常  {}",gameId,type);
    			}
    			continue;
    		}
        	if(type == 1){//对子
        		List<PokerCard> listCard = pokerStore.getCouplePoker();
        		if(listCard.size() != 2){//不够了
        			type = 2;
        		}else{
        			logger.info("{}  对子  {}",gameId,listCard);
        			allHandCards.add(listCard);
        			continue;
        		}
        	}
        	if(type == 2){//顺子
        		List<PokerCard> listCard = pokerStore.getStraight();
        		if(listCard.size() == 3){
        			logger.info("{}  顺子 {}",gameId,listCard);
        			allHandCards.add(listCard);
        			continue;
        		}else{
        			type = 3;
        		}
        	}
        	if(type == 3){//金花
        		List<PokerCard> listCard = pokerStore.getGoldflower();
        		if(listCard.size() == 3){
        			logger.info("{}  金花 {}",gameId,listCard);
        			allHandCards.add(listCard);
        			continue;
        		}
        	}
		}
		//剩余牌补全
		for (List<PokerCard> listCard : allHandCards) {
			if(listCard.size() == 1){//A大单张
				int val = listCard.get(0).getZjhPokerValue();
				for (int i = 0; i < 2; i++) {
					PokerCard card = pokerStore.get1LessValPoker(val);
					if(card == null){
						card = pokerStore.get1Poker();
						if(card == null){
							logger.error("{}  配牌异常  {}",gameId,val);
							return mixedCard;
						}
					}
					listCard.add(card);
				}
			}else if(listCard.size() == 2){//对子
				int val = listCard.get(0).getZjhPokerValue();
				PokerCard card = pokerStore.get1NoEqualValPoker(val);
				if(card == null){
					card = pokerStore.get1Poker();
				}
				listCard.add(card);
			}
		}
		
		while(allHandCards.size() > 0){
			List<PokerCard> handCards = allHandCards.remove(Randomizer.nextInt(allHandCards.size()));
			logger.info("{}最终牌型{}",gameId,handCards);
			result.addAll(handCards);
		}
		
		allPoker.removeAll(result);
		result.addAll(allPoker);
    	return result;
	}
	
	/** 配置看牌牛牛牌型 */
	public static Queue<PokerCard> matchNiuCard2(long gameId,Queue<PokerCard> mixedCard,ConfMatchCard confMatchNiuCard,int joinPlayerCount){
		if(joinPlayerCount > 5){
			return mixedCard;
		}
    	Set<PokerCard> allPoker = new HashSet<>(mixedCard);
    	Queue<PokerCard> result = new LinkedList<>();
    	List<List<PokerCard>> allHandCards = new ArrayList<>();
    	PokerStore pokerStore = new PokerStore(allPoker);
    	Map<Integer, Integer> indexTypeMap = new HashMap<>();
    	for (int i = 0; i < joinPlayerCount; i++) {
    		int type = confMatchNiuCard.getGrabNiuType();
    		List<PokerCard> list4Card = new ArrayList<>();
    		if(type == 0){//四张牌为10、J、Q、K的组合
    			list4Card = pokerStore.get4_10Poker();
    			if(list4Card.size() != 4){//不够了
        			type = Randomizer.nextInt(1, 3);
    			}else{
        			logger.info("{}  四张牌为10、J、Q、K的组合  {}",gameId,list4Card);
        			allHandCards.add(list4Card);
        		}
    		}
    		indexTypeMap.put(i, type);
    	}
    	for (int i = 0; i < joinPlayerCount; i++) {
    		int type = indexTypeMap.get(i);
    		if(type == -1 || type == 0){
    			if(type == -1){
    				logger.error("{}  随机种类异常  {}",gameId,type);
    			}
    			continue;
    		}
    		List<PokerCard> list4Card = new ArrayList<>();
        	if(type == 1){//4张牌有牛，且之和10的倍数
        		list4Card = pokerStore.get4_Equal10Poker();
        		if(list4Card.size() != 4){//不够了
        			type = 2;
        		}else{
        			logger.info("{}  4张牌有牛，且之和10的倍数  {}",gameId,list4Card);
        		}
        	}
        	if(type == 2){//4张牌有牛，且之和不是10的倍数
        		list4Card = pokerStore.get4_NoEqual10Poker();
        		logger.info("{}  4张牌有牛，且之和不是10的倍数  {}",gameId,list4Card);
        	}
        	if(type == 1 || type == 2){
        		if(list4Card.size() == 4){
            		allHandCards.add(list4Card);
            	}else{
            		logger.error("{}配牌失败",gameId);
            	}
        	}
		}
    	
    	while(allHandCards.size() > 0){
			List<PokerCard> handCards = allHandCards.remove(Randomizer.nextInt(allHandCards.size()));
			PokerCard card = pokerStore.get1Poker();
			if(card == null){
				logger.error("{}配牌失败1",gameId);
				return mixedCard;
			}
			handCards.add(card);
			logger.info("{}最终牌型{}",gameId,handCards);
			result.addAll(handCards);
		}
		
		allPoker.removeAll(result);
		result.addAll(allPoker);
    	return result;
	}
	
	/** 配置经典牛牛牌型 */
	public static Queue<PokerCard> matchNiuCard(long gameId,Queue<PokerCard> mixedCard,ConfMatchCard confMatchNiuCard,int joinPlayerCount){
		if(joinPlayerCount > 5){
			return mixedCard;
		}
    	Set<PokerCard> allPoker = new HashSet<>(mixedCard);
    	Queue<PokerCard> result = new LinkedList<>();
    	//提升分为三个档次，提升至（牛8、牛9、牛牛）
    	Pair<NiuResult, List<PokerCard>> maxNiuResult = null;
    	//各家牌大小和位置
    	List<NiuPosCard> niuPosCardList = new ArrayList<>();
    	//牛数相同时个数
    	Map<NiuResult, Integer> repetedNiuNumMap = new HashMap<>();
    	for (int i = 0; i < joinPlayerCount; i++) {
    		List<PokerCard> handCards = new ArrayList<>(5);
    		for (int j = 0; j < 5; j++) {
    			handCards.add(mixedCard.poll());
			}
    		result.addAll(handCards);
    		Pair<NiuResult, List<PokerCard>> niuResult  = new Pair<>(PokerUtil.calNiuResult(handCards).getRight(), handCards);
    		if(maxNiuResult == null || PokerUtil.isOneGtTwo(niuResult,maxNiuResult)){
    			maxNiuResult = niuResult;
    		}
    		niuPosCardList.add(new NiuPosCard(i, niuResult));
    		Integer repetedNiuNum =  repetedNiuNumMap.get(niuResult.getLeft()); 
    		if(repetedNiuNum == null){
    			repetedNiuNum = 0;
    		}
    		repetedNiuNum++;
    		repetedNiuNumMap.put(niuResult.getLeft(), repetedNiuNum);
		}
    	if(maxNiuResult.getLeft().getValue() >= NiuResult.NIU_NIU.getValue()){
    		return result;
    	}
    	
    	result.clear();
    	logger.info("{}没有存在牛牛之上的牌,可能需要进行配牌",gameId);
    	int niuVal = confMatchNiuCard.getMatchNiu(maxNiuResult.getLeft().getValue());
    	if(niuVal == -1){
    		return result;
    	}
    	Collections.sort(niuPosCardList,NIU_COMPARATOR);
    	logger.info("{}最大牛{},提升到牛牌 {}",gameId,maxNiuResult.getLeft(),niuVal);
		//开始提升 点数
		int needUpNiuNum = niuVal - maxNiuResult.getLeft().getValue();
		
		List<List<PokerCard>> allHandCards = new ArrayList<>();

		PokerStore pokerStore = new PokerStore(allPoker);
		for (Entry<NiuResult, Integer> entry : repetedNiuNumMap.entrySet()) {
			//提升点数后牛的数目
			int newNiuNum = entry.getKey().getValue() + needUpNiuNum;
			Integer repetedNiuNum =  entry.getValue();
			//提升点数0-->0-2
			logger.info("{}提升{}点数{}-->{} {}家",gameId,needUpNiuNum,entry.getKey().getValue(),newNiuNum,repetedNiuNum);
			for (int i = 1; i <= repetedNiuNum; i++) {
				//为了打乱
				List<PokerCard> handCards = new ArrayList<>(5);
				List<PokerCard> list3 = pokerStore.get3_10Poker_equal0();
				if(list3.size() != 3){
					result.addAll(allPoker);
					logger.error("配牌失败,自动返回原牌型");
					return result;
				}
				handCards.addAll(list3);
				List<PokerCard> list2 = pokerStore.get2_numPoker_less10(newNiuNum);
				if(list2.size() != 2){
					result.addAll(allPoker);
					logger.error("配牌失败,自动返回原牌型");
					return result;
				}
				handCards.addAll(list2);
				allHandCards.add(handCards);
			}
		}
		
		//最新各家牌大小和位置
    	List<NiuPosCard> newNiuPosCardList = new ArrayList<>();
		
    	for (List<PokerCard> handCards : allHandCards) {
    		pokerStore.change3_10Poker(handCards);
    		Pair<NiuResult, List<PokerCard>> niuResult  = new Pair<>(PokerUtil.calNiuResult(handCards).getRight(), handCards);
			newNiuPosCardList.add(new NiuPosCard(1, niuResult));
    		logger.info("{}最终牌型{}",gameId,handCards);
		}
    	Collections.sort(newNiuPosCardList,NIU_COMPARATOR);
    	//维持输赢关系
    	if(newNiuPosCardList.size() == niuPosCardList.size()){
    		for (int i = 0; i < niuPosCardList.size(); i++) {
    			NiuPosCard niuPosCard = niuPosCardList.get(i);
    			niuPosCard.niuResult = newNiuPosCardList.get(i).niuResult;
			}
    		Collections.sort(niuPosCardList,POS_COMPARATOR);
    		for (NiuPosCard niuPosCard : niuPosCardList) {
    			result.addAll(niuPosCard.niuResult.getRight());
			}
    	}
		
		allPoker.removeAll(result);
		result.addAll(allPoker);
    	return result;
    } 
	
	public static Comparator<NiuPosCard> NIU_COMPARATOR = (e, f) -> PokerUtil.isOneGtTwo(e.niuResult,f.niuResult) ? 1 : -1;
	public static Comparator<NiuPosCard> POS_COMPARATOR = (e, f) -> e.pos - f.pos;
	
	private static class NiuPosCard{
		int pos;
		Pair<NiuResult, List<PokerCard>> niuResult;

		public NiuPosCard(int pos, Pair<NiuResult, List<PokerCard>> niuResult) {
			super();
			this.pos = pos;
			this.niuResult = niuResult;
		}
	}
	
	private static class PokerStore{
		private List<PokerCard> allCards = new ArrayList<PokerCard>();
		
		public PokerStore(Collection<PokerCard> pokers) {
			allCards.addAll(pokers);
		}
		
		private void removePoker(PokerCard card){
			allCards.remove(card);
		}
		
		public PokerCard get1EqualValPoker(int val){
			for (int i = 0; i < allCards.size(); i++) {
				PokerCard card1 = allCards.get(i);
				if(card1.getValue() == val){
					removePoker(card1);
					return card1;
				}
			}
			return null;
		}
		
		public List<PokerCard> get3_10Poker_equal0(){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				PokerCard card = get1EqualValPoker(0);
				list.add(card);
			}
			return list;
		}
		
		public List<PokerCard> get4_10Poker(){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < 4; i++) {
				PokerCard card = get1EqualValPoker(0);
				list.add(card);
			}
			return list;
		}
		
		public List<PokerCard> get2_numPoker_less10(int num){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < allCards.size() - 1; i++) {
				for (int j = i + 1; j < allCards.size(); j++) {
					PokerCard card1 = allCards.get(i);
					PokerCard card2 = allCards.get(j);
					if(card1.getValue() == 0 || card2.getValue() == 0){
						continue;
					}
					int curNum = card1.getValue() + card2.getValue();
					boolean find = false;
					if(num == 10){
						if(curNum % 10 ==  0){
							find = true;
						}
					}else{
						if(curNum % 10 ==  num){
							find = true;
						}
					}
					if(find){
						removePoker(card1);
						removePoker(card2);
						list.add(card1);
						list.add(card2);
						return list;
					}
				}
			}
			return list;
		}
		
		
		private List<PokerCard> get2_numPoker(int num,List<PokerCard> cards){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < allCards.size() - 1; i++) {
				for (int j = i + 1; j < allCards.size(); j++) {
					PokerCard card1 = allCards.get(i);
					PokerCard card2 = allCards.get(j);
					
					int curNum = card1.getValue() + card2.getValue();
					boolean find = false;
					if(num == 10){
						if(curNum % 10 ==  0){
							find = true;
						}
					}else{
						if(curNum % 10 ==  num){
							find = true;
						}
					}
					if(find){
						List<PokerCard> cards5 = new ArrayList<>(cards);
						cards5.remove(0);
						cards5.remove(0);
						cards5.add(card1);
						cards5.add(card2);
						//过滤四张
						Map<Integer, Integer> cardCounts = PokerUtil.getAllPokerCount(cards5);
						for (Integer count : cardCounts.values()) {
							if(count >= 4){
								find = false;
								break;
							}
						}
					}
					if(find){
						removePoker(card1);
						removePoker(card2);
						list.add(card1);
						list.add(card2);
						return list;
					}
				}
			}
			return list;
		}
		
		private List<PokerCard> get3_numPoker(int num,List<PokerCard> cards){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < allCards.size() - 2; i++) {
				for (int j = i + 1; j < allCards.size() - 1; j++) {
					for (int z = j + 1; z < allCards.size(); z++) {
						PokerCard card1 = allCards.get(i);
						PokerCard card2 = allCards.get(j);
						PokerCard card3 = allCards.get(z);
						
						int curNum = card1.getValue() + card2.getValue() + card3.getValue();
						boolean find = false;
						if(num == 10){
							if(curNum % 10 ==  0){
								find = true;
							}
						}else{
							if(curNum % 10 ==  num){
								find = true;
							}
						}
						if(find){
							List<PokerCard> cards5 = new ArrayList<>(cards);
							cards5.remove(0);
							cards5.remove(0);
							cards5.remove(0);
							cards5.add(card1);
							cards5.add(card2);
							cards5.add(card3);
							//过滤四张
							Map<Integer, Integer> cardCounts = PokerUtil.getAllPokerCount(cards5);
							for (Integer count : cardCounts.values()) {
								if(count >= 4){
									find = false;
									break;
								}
							}
						}
						if(find){
							removePoker(card1);
							removePoker(card2);
							removePoker(card3);
							list.add(card1);
							list.add(card2);
							list.add(card3);
							return list;
						}
					}
				}
			}
			return list;
		}
		
		private List<PokerCard> get4_Equal10Poker(){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < allCards.size() - 3; i++) {
				for (int j = i + 1; j < allCards.size() - 2; j++) {
					for (int z = j + 1; z < allCards.size() - 1; z++) {
						for (int k = z + 1; k < allCards.size(); k++) {
							PokerCard card1 = allCards.get(i);
							PokerCard card2 = allCards.get(j);
							PokerCard card3 = allCards.get(z);
							PokerCard card4 = allCards.get(k);
							
							List<PokerCard> list4 = new ArrayList<>();
							list4.add(card1);
							list4.add(card2);
							list4.add(card3);
							list4.add(card4);
							if(hasNiu(list4)){
								int curNum = card1.getValue() + card2.getValue() + card3.getValue() + card4.getValue();
								if(curNum % 10 == 0){
									removePoker(card1);
									removePoker(card2);
									removePoker(card3);
									removePoker(card4);
									list.add(card1);
									list.add(card2);
									list.add(card3);
									list.add(card4);
									return list;
								}
							}
						}
					}
				}
			}
			return list;
		}
		
		private boolean hasNiu(List<PokerCard> list4){
			for (int i = 0; i < list4.size() - 2; i++) {
				for (int j = i+1; j < list4.size() - 1; j++) {
					for (int z = j+1; z < list4.size(); z++) {
						PokerCard card1 = list4.get(i);
						PokerCard card2 = list4.get(j);
						PokerCard card3 = list4.get(z);
						int curNum = card1.getValue() + card2.getValue() + card3.getValue();
						if(curNum % 10 == 0){
							return true;
						}
						
					}
				}
			}
			return false;
		}
		
		private List<PokerCard> get4_NoEqual10Poker(){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < allCards.size() - 3; i++) {
				for (int j = i + 1; j < allCards.size() - 2; j++) {
					for (int z = j + 1; z < allCards.size() - 1; z++) {
						for (int k = z + 1; k < allCards.size(); k++) {
							PokerCard card1 = allCards.get(i);
							PokerCard card2 = allCards.get(j);
							PokerCard card3 = allCards.get(z);
							PokerCard card4 = allCards.get(k);
							
							//三张牌中必须存在牛
							List<PokerCard> list4 = new ArrayList<>();
							list4.add(card1);
							list4.add(card2);
							list4.add(card3);
							list4.add(card4);
							if(hasNiu(list4)){
								int curNum = card1.getValue() + card2.getValue() + card3.getValue() + card4.getValue();
								if(curNum % 10 != 0){
									removePoker(card1);
									removePoker(card2);
									removePoker(card3);
									removePoker(card4);
									list.add(card1);
									list.add(card2);
									list.add(card3);
									list.add(card4);
									return list;
								}
							}
						}
					}
				}
			}
			return list;
		}
		
		public List<PokerCard> change3_10Poker(List<PokerCard> cards){
			int rand = Randomizer.nextInt(3);
			if(rand == 0){//换一张
				//不换
			}else if(rand == 1){//换两张
				List<PokerCard> listCard = get2_numPoker(10,cards);
				if(listCard.size() == 2){
					cards.remove(0);
					cards.remove(0);
					cards.addAll(listCard);
				}
			}else{//换三张
				List<PokerCard> listCard = get3_numPoker(10,cards);
				if(listCard.size() == 3){
					cards.remove(0);
					cards.remove(0);
					cards.remove(0);
					cards.addAll(listCard);
				}
			}
			Collections.shuffle(cards);
			return cards;
		}
		
		
		public PokerCard get1Poker(){
			if(allCards.size() == 0){
				return null;
			}
			int index = Randomizer.nextInt(allCards.size());
			PokerCard card = allCards.get(index);
			removePoker(card);
			return card;
		}
		
		public PokerCard get1APoker(){
			for (int i = 0; i < allCards.size(); i++) {
				PokerCard card1 = allCards.get(i);
				if(card1.getValue() == 1){
					removePoker(card1);
					return card1;
				}
			}
			return null;
		}
		
		public List<PokerCard> getCouplePoker(){
			List<PokerCard> listCard = new ArrayList<>();
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < allCards.size() - 1; i++) {
				for (int j = i + 1; j < allCards.size(); j++) {
					PokerCard card1 = allCards.get(i);
					PokerCard card2 = allCards.get(j);
					
					if(card1.getPokerValue() == card2.getPokerValue()){
						listCard.add(card1);
						listCard.add(card2);
						removePoker(card1);
						removePoker(card2);
						return list;
					}
				}
			}
			return list;
		}
		
		public List<PokerCard> getStraight(){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < allCards.size() - 2; i++) {
				for (int j = i + 1; j < allCards.size() - 1; j++) {
					for (int z = j + 1; z < allCards.size(); z++) {
						PokerCard card1 = allCards.get(i);
						PokerCard card2 = allCards.get(j);
						PokerCard card3 = allCards.get(z);
						list.clear();
						list.add(card1);
						list.add(card2);
						list.add(card3);
						if (!PokerUtil.checkPokerColorIsTheSame(list)) {
							if(PokerUtil.checkZjhStraight(list)){
								removePoker(card1);
								removePoker(card2);
								removePoker(card3);
								return list;
							}
						}
						list.clear();
					}
				}
			}
			return list;
		}
		
		public List<PokerCard> getGoldflower(){
			List<PokerCard> list = new ArrayList<>();
			for (int i = 0; i < allCards.size() - 2; i++) {
				for (int j = i + 1; j < allCards.size() - 1; j++) {
					for (int z = j + 1; z < allCards.size(); z++) {
						PokerCard card1 = allCards.get(i);
						PokerCard card2 = allCards.get(j);
						PokerCard card3 = allCards.get(z);
						list.clear();
						list.add(card1);
						list.add(card2);
						list.add(card3);
						
						if(!PokerUtil.checkZjhStraight(list)){
							Collections.sort(list, PokerUtil.POKER_ZJH);
							if (PokerUtil.checkPokerColorIsTheSame(list)) {
								removePoker(card1);
								removePoker(card2);
								removePoker(card3);
								return list;
							}
						}
						list.clear();
					}
				}
			}
			return list;
		}
		
		public PokerCard get1LessValPoker(int val){
			for (int i = 0; i < allCards.size(); i++) {
				PokerCard card1 = allCards.get(i);
				if(card1.getZjhPokerValue() < val){
					removePoker(card1);
					return card1;
				}
			}
			return null;
		}
		
		public PokerCard get1NoEqualValPoker(int val){
			for (int i = 0; i < allCards.size(); i++) {
				PokerCard card1 = allCards.get(i);
				if(card1.getZjhPokerValue() != val){
					removePoker(card1);
					return card1;
				}
			}
			return null;
		}
	}
}
