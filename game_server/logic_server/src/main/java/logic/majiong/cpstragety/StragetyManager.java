package logic.majiong.cpstragety;

import logic.majiong.CoupleFanType;

import java.util.*;

public class StragetyManager {
	private static StragetyManager ourInstance = new StragetyManager();

	public static StragetyManager getInstance() {
		return ourInstance;
	}

	private StragetyManager() {
	}

	private Map<CoupleFanType, IStragetyHandler> handlerMap = new HashMap<>();
    private Map<CoupleFanType, IStragetyHandler> tingHandlerMap = new HashMap<>();

	public void registerAllStragety() {
		handlerMap.clear();
        tingHandlerMap.clear();
		registerOneStragety(CoupleFanType.ONE_1, OneStragety::one);
		registerOneStragety(CoupleFanType.ONE_2, OneStragety::two);
		registerOneStragety(CoupleFanType.ONE_3, OneStragety::three);
		registerOneStragety(CoupleFanType.ONE_4, OneStragety::four);
		registerOneStragety(CoupleFanType.ONE_5, OneStragety::five);
		registerOneStragety(CoupleFanType.ONE_6, OneStragety::six);
		registerOneStragety(CoupleFanType.ONE_7, OneStragety::seven);
		registerOneStragety(CoupleFanType.ONE_8, OneStragety::eight);
		registerOneStragety(CoupleFanType.ONE_9, OneStragety::nine);
		registerOneStragety(CoupleFanType.TWO_1, TwoStragety::one);
		registerOneStragety(CoupleFanType.TWO_2, TwoStragety::two);
		registerOneStragety(CoupleFanType.TWO_3, TwoStragety::three);
		registerOneStragety(CoupleFanType.TWO_4, TwoStragety::four);
		registerOneStragety(CoupleFanType.TWO_5, TwoStragety::five);
		registerOneStragety(CoupleFanType.TWO_6, TwoStragety::six);
		registerOneStragety(CoupleFanType.TWO_7, TwoStragety::seven);
		registerOneStragety(CoupleFanType.TWO_8, TwoStragety::eight);
		registerOneStragety(CoupleFanType.TWO_9, TwoStragety::nine);
		registerOneStragety(CoupleFanType.TWO_10, TwoStragety::ten);
		registerOneStragety(CoupleFanType.FOUR_1, FourStragety::one);
		registerOneStragety(CoupleFanType.FOUR_2, FourStragety::two);
		registerOneStragety(CoupleFanType.FOUR_3, FourStragety::three);
		registerOneStragety(CoupleFanType.FOUR_4, FourStragety::four);
		registerOneStragety(CoupleFanType.SIX_1, SixStragety::one);
		registerOneStragety(CoupleFanType.SIX_2, SixStragety::two);
		registerOneStragety(CoupleFanType.SIX_3, SixStragety::three);
		registerOneStragety(CoupleFanType.SIX_4, SixStragety::four);
		registerOneStragety(CoupleFanType.SIX_5, SixStragety::five);
		registerOneStragety(CoupleFanType.SIX_6, SixStragety::six);
		registerOneStragety(CoupleFanType.EIGHT_1, EightStragety::one);
		registerOneStragety(CoupleFanType.EIGHT_2, EightStragety::two);
		registerOneStragety(CoupleFanType.EIGHT_3, EightStragety::three);
		registerOneStragety(CoupleFanType.EIGHT_4, EightStragety::four);
		registerOneStragety(CoupleFanType.SIXTEEN_1, SixteenStragety::one);
		registerOneStragety(CoupleFanType.SIXTEEN_2, SixteenStragety::two);
		registerOneStragety(CoupleFanType.SIXTEEN_3, SixteenStragety::three);
		registerOneStragety(CoupleFanType.SIXTEEN_4, SixteenStragety::four);
		registerOneStragety(CoupleFanType.SIXTEEN_5, SixteenStragety::five);
		registerOneStragety(CoupleFanType.TWENTY_FOUR_1, TwentyFourStragety::one);
		registerOneStragety(CoupleFanType.TWENTY_FOUR_2, TwentyFourStragety::two);
		registerOneStragety(CoupleFanType.TWENTY_FOUR_3, TwentyFourStragety::three);
		registerOneStragety(CoupleFanType.TWENTY_FOUR_4, TwentyFourStragety::four);
		registerOneStragety(CoupleFanType.TWENTY_FOUR_5, TwentyFourStragety::five);
		registerOneStragety(CoupleFanType.THIRTY_TWO_1, ThirtyTwoStragety::one);
		registerOneStragety(CoupleFanType.THIRTY_TWO_2, ThirtyTwoStragety::two);
		registerOneStragety(CoupleFanType.THIRTY_TWO_3, ThirtyTwoStragety::three);
		registerOneStragety(CoupleFanType.THIRTY_TWO_4, ThirtyTwoStragety::four);
		registerOneStragety(CoupleFanType.FORTY_EIGHT_1, FortyEightStragety::one);
		registerOneStragety(CoupleFanType.FORTY_EIGHT_2, FortyEightStragety::two);
		registerOneStragety(CoupleFanType.FORTY_EIGHT_3, FortyEightStragety::three);
		registerOneStragety(CoupleFanType.FORTY_EIGHT_4, FortyEightStragety::four);
		registerOneStragety(CoupleFanType.SIXTY_FOUR_1, SixtyFourStragety::one);
		registerOneStragety(CoupleFanType.SIXTY_FOUR_2, SixtyFourStragety::two);
		registerOneStragety(CoupleFanType.SIXTY_FOUR_3, SixtyFourStragety::three);
		registerOneStragety(CoupleFanType.SIXTY_FOUR_4, SixtyFourStragety::four);
		registerOneStragety(CoupleFanType.SIXTY_FOUR_5, SixtyFourStragety::five);
		registerOneStragety(CoupleFanType.SIXTY_FOUR_6, SixtyFourStragety::six);
		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_1, EightyEightStragety::one);
		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_2, EightyEightStragety::two);
//		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_3, EightyEightStragety::three);
		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_4, EightyEightStragety::four);
		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_5, EightyEightStragety::five);
		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_6, EightyEightStragety::six);
		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_7, EightyEightStragety::seven);
		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_8, EightyEightStragety::eight);
		registerOneStragety(CoupleFanType.EIGHTY_EIGHT_9, EightyEightStragety::nine);
        registerOneStragety(CoupleFanType.EIGHTY_EIGHT_10,EightyEightStragety::ten);
	}

	public void registerOneStragety(CoupleFanType type, IStragetyHandler handler) {
		handlerMap.put(type, handler);
		if (type != CoupleFanType.EIGHTY_EIGHT_9 && type != CoupleFanType.EIGHTY_EIGHT_8 && type != CoupleFanType.EIGHTY_EIGHT_10 && type != CoupleFanType.THIRTY_TWO_3 && type != CoupleFanType.SIX_4 && type != CoupleFanType.FOUR_2 && type != CoupleFanType.TWO_8 && type != CoupleFanType.ONE_5) {
			tingHandlerMap.put(type, handler);
		}
	}

	public List<CoupleFanType> checkValidFanType(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		List<CoupleFanType> resultTypeList = new ArrayList<>();
		for (Map.Entry<CoupleFanType, IStragetyHandler> entry : handlerMap.entrySet()) {
			if (entry.getValue().handler(gang, ke, chi, remain)) {
				resultTypeList.add(entry.getKey());
			}
		}
		return resultTypeList;
	}

    public List<CoupleFanType> checkValidFanTypeTing(Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
        List<CoupleFanType> resultTypeList = new ArrayList<>();
        for (Map.Entry<CoupleFanType, IStragetyHandler> entry : tingHandlerMap.entrySet()) {
            if (entry.getValue().handler(gang, ke, chi, remain)) {
                resultTypeList.add(entry.getKey());
            }
        }
        return resultTypeList;
    }
	
}
