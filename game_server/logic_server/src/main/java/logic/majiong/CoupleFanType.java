package logic.majiong;

import java.util.Arrays;
import java.util.List;

public enum CoupleFanType {
	ONE_1(0x0101, "1：一般高"),                      //1：一般高 由一种花色2副相同的顺子组成的牌
	ONE_2(0x0102, "2：连六"),                      //2：连六  一种花色6张相连续的序数牌
	ONE_3(0x0103, "3：老少副"),                      //3：老少副 一种花色牌的123,789两副顺子
	ONE_4(0x0104, "4：花牌"),                      //4：花牌 即春，夏，秋，东，梅，兰，竹，菊八张花牌。每花一番，不计入起和番内，和牌后才能计番
	ONE_5(0x0105, "5：明杠"),                      //5：明杠  自己有暗刻，碰别人打出的一张相同牌开杠。或自己抓进一张与碰的明刻相同的牌开杠。
	ONE_6(0x0106, "6：边张"),                      //6：边张  单和123的3和789的7或1233和3,77879和7都为边张。手中有12345和3,56789和7不算边张
	ONE_7(0x0107, "7：坎张"),                      //7：坎张 和2张中间的牌。4556和5也为坎张，手中有45567和6不算坎张。
	ONE_8(0x0108, "8：单钓将"),                      //8：单钓将 钓单张作将和牌。
	ONE_9(0x0109, "9：自摸"),                      //9：自摸 自己抓进牌成和牌。
	TWO_1(0x0201, "1：门风刻"),                      //1：门风刻 与本门风相同的风刻
	TWO_2(0x0202, "2：圈风刻"),                      //2：圈风刻与圈风相同的风刻
	TWO_3(0x0203, "3：箭刻"),                      //3：箭刻 由中，发，白3张相同的牌组成的刻子
	TWO_4(0x0204, "4：平和 "),                      //4：平和 由4副顺子及序数牌组成的和牌，边，坎，钓不影响平和
	TWO_5(0x0205, "5：四归一"),                      //5：四归一 和牌中，有4张相同的牌归于一家的顺，刻子，对，将牌中，杠除外。
	TWO_6(0x0206, "6：断幺"),                      //6：断幺 和牌中没有一，九及字牌。
	TWO_7(0x0207, "7: 双暗刻"),                      //7: 双暗杠和牌中有两个暗刻
	TWO_8(0x0208, "8：暗杠"),                      //8：门前清 没有吃，碰，明杠，和别人打出的牌
	TWO_9(0x0209, "9：门前清"),                      //9：报听 主动选择听牌，自动摸打之后和牌。
	TWO_10(0x0210, "10 报听"),                      //10：暗杠
	FOUR_1(0x0401, "1：全带幺"),                     //1：全带幺 和牌时，每副牌，将牌都带有幺牌。
	FOUR_2(0x0402, "2：双明杠", Arrays.asList(ONE_5)),                     //2：双明杠 2个明杠，不计明杠。
	FOUR_3(0x0403, "3：不求人", Arrays.asList(ONE_9, TWO_8)),                     //3：不求人 4副牌及将中没有吃，碰，杠。自摸和牌。不计自摸，门前清。
	FOUR_4(0x0404, "4：和绝张"),                     //4：和绝张 和牌池，桌面已经亮明3张牌的第4张。（抢杠和不计和绝张）
	FOUR_5(0x0405, "5：报听一发"),                     //5：报听一发
	FOUR_6(0x0406, "6：无花牌"),                     //6:无花牌
	SIX_1(0x0601, "1：小三风"),                      //1：小三风 东，南，西，北其中2个是刻子，1个是对，和牌时有两个风牌的刻或杠，并且将牌为风牌。
	SIX_2(0x0602, "2：双箭刻", Arrays.asList(TWO_3)),                      //2：双箭刻 2副箭刻或杠
	SIX_3(0x0603, "3：碰碰和"),                      //3：碰碰和 由4个刻子或杠组成的和牌
	SIX_4(0x0604, "4：双暗杠", Arrays.asList(TWO_8)),                      //4：双暗杠 2个暗杠
	SIX_5(0x0605, "5：混一色"),                      //5：混一色 由一种花色序数牌及字牌组成的和牌
	SIX_6(0x0606, "6：全求人"),                      //6：全求人 全靠吃牌，碰牌，单钓别人打出的牌和牌。
	EIGHT_1(0x0801, "1：妙手回春", Arrays.asList(ONE_9)),                    //1：妙手回春 自摸牌墙上最后一张牌和牌，不计自摸。
	EIGHT_2(0x0802, "2：海底捞月"),                    //2：海底捞月和打出的最后一张牌。
	EIGHT_3(0x0803, "3: 杠上开花", Arrays.asList(ONE_9)),                    //3: 杠上开花开杠抓进的牌成和牌（不包括补花），不计自摸。
	EIGHT_4(0x0804, "4：抢杠和", Arrays.asList(FOUR_4)),                    //4：抢杠和 和别人自抓开明杠的牌，不计和绝张。
	SIXTEEN_1(0x1001, "1：清龙", Arrays.asList(ONE_2, ONE_3)),                  //1：清龙 和牌时，有一种花色1-9相连的序数牌，不计连六，老少副。
	SIXTEEN_2(0x1002, "2：三步高"),                  //2：三步高 和牌时，有一种花色3副依次递增或递减一位数字的顺子。
	SIXTEEN_3(0x1003, "3：全花摸"),                  //3：全花摸到全部八张花牌。不计花牌单独番数。
	SIXTEEN_4(0x1004, "4：三暗刻", Arrays.asList(TWO_7)),                  //4：三暗刻 3个暗刻，不计双暗刻
	SIXTEEN_5(0x1005, "5：清一色"),                  //5：清一色 是指由1种花色的序数牌组成的和牌。清一色不计无字，缺一门。
	TWENTY_FOUR_1(0x1801, "1：四字刻", Arrays.asList(SIX_3)),              //1：四字刻四个字牌的刻或杠，不计碰碰和。
	TWENTY_FOUR_2(0x1802, "2：大三风 ", Arrays.asList(SIX_1)),              //2：大三风 3个风刻，不计小三风。
	TWENTY_FOUR_3(0x1803, "3：三同顺", Arrays.asList(ONE_1)),              //3：三同顺 和牌时有一种花色3副序数相同的顺子。不计三连刻，一般高。
	TWENTY_FOUR_4(0x1804, "4：七对子", Arrays.asList(ONE_8, ONE_9, TWO_9)),              //4：七对子 由7个对子组成的和牌，不计门前清，单钓将，自摸。
	TWENTY_FOUR_5(0x1805, "5：三连刻", Arrays.asList(ONE_1, TWENTY_FOUR_3)),              //5：三连刻 和牌时有一种花色3副依次递增一位数的刻子。不计三同顺，一般高。
	THIRTY_TWO_1(0x2001, "1：四步高", Arrays.asList(ONE_1, ONE_9, SIXTEEN_2)),               //1：四步高 一种花色4副依次递增一位数的顺子，不计三步高，连六，老少副。
	THIRTY_TWO_2(0x2002, "2：混幺九", Arrays.asList(FOUR_1, SIX_3)),               //2：混幺九 由字牌和序数为一，九的刻子，顺子，组成的和牌。不计碰碰和，全带幺。
	THIRTY_TWO_3(0x2003, "3：三杠", Arrays.asList(ONE_5, FOUR_2)),               //3：三杠 3个杠。不计双明杠，明杠。
	THIRTY_TWO_4(0x2004, "4：天听"),               //4：天听 庄家打出第一张牌后报听为天听，发完牌后闲家便报听也称天听。天听和牌后计番。补花之后听牌，也算天听。
	FORTY_EIGHT_1(0x3001, "1：四同顺", Arrays.asList(ONE_1, TWO_5, TWENTY_FOUR_3, TWENTY_FOUR_4, TWENTY_FOUR_5)),              //1：四同顺 一种花色4副序数相同的顺子。不计三连刻，三同顺，七对，四归一，一般高。
	FORTY_EIGHT_2(0x3002, "2：三元七对子", Arrays.asList(TWENTY_FOUR_4, TWO_9, ONE_9, ONE_7)),              //2：三元七对子 和牌为七对子，其中包含中，发，白三种自牌。不计七对，门前清，单钓将，自摸
	FORTY_EIGHT_3(0x3003, "3：四喜七对子", Arrays.asList(TWENTY_FOUR_4, ONE_7, ONE_9, TWO_9)),              //3：四喜七对子 和牌为七对子，并且包含东，南，西，北。不计七对，门前清，单钓将，自摸。
	FORTY_EIGHT_4(0x3004, "4：四连刻", Arrays.asList(ONE_1, SIX_3, TWENTY_FOUR_3, TWENTY_FOUR_5)),              //4：四连刻 一种花色4副依次递增一位数的刻子。不计三连刻，三同顺，碰碰和，一般高。
	SIXTY_FOUR_1(0x4001, "1：小四喜", Arrays.asList(SIX_1, TWENTY_FOUR_2, TWO_1, TWO_2)),               //1：小四喜 和牌时有风牌的3副刻子及将牌。不计大三风，小三风，圈风刻，门风刻。
	SIXTY_FOUR_2(0x4002, "2：小三元", Arrays.asList(TWO_3, SIX_2)),               //2：小三元 和牌时有箭牌的两副刻子及将牌。不计双箭刻，箭刻。
	SIXTY_FOUR_3(0x4003, "3：四暗刻", Arrays.asList(TWO_7, SIXTEEN_4, SIX_3, TWO_9, ONE_9)),               //3：四暗刻 4个暗刻（暗杠）。不计三暗刻，双暗刻，碰碰和，门前清，自摸。
	SIXTY_FOUR_4(0x4004, "4：双龙会", Arrays.asList(ONE_1, ONE_3, TWENTY_FOUR_4, SIXTEEN_5, TWO_4)),               //4：双龙会 一种花色的两个老少副，且5为将牌。不计一般高，七对，清一色，老少副，平和。
	SIXTY_FOUR_5(0x4005, "5：字一色", Arrays.asList(SIX_3, FOUR_1, THIRTY_TWO_2, TWENTY_FOUR_1)),               //5：字一色 没有数字牌，全部为字牌。不计碰碰和，全带幺，混幺九，四字刻。
	SIXTY_FOUR_6(0x4006, "6：人和"),               //6：人和 庄家打出第一张牌闲家就和牌称为人和，如果庄家出牌前有暗杠，那么不算人和。
	EIGHTY_EIGHT_1(0x5801, "1：大四喜", Arrays.asList(TWO_1, TWO_2, SIX_1, SIX_3, TWENTY_FOUR_1, TWENTY_FOUR_2)),             //1：大四喜 由4副风刻（杠）组成的和牌。不计碰碰和，圈风刻，门风刻，大三风，小三风，四字刻。
	EIGHTY_EIGHT_2(0x5802, "2：大三元", Arrays.asList(TWO_3, SIX_2)),             //2：大三元 和牌中，有中，发，白3副刻子。不计双箭刻，箭刻。
	EIGHTY_EIGHT_3(0x5803, "3：九莲宝灯", Arrays.asList(SIXTEEN_5, TWO_9, ONE_8)),             //3：九莲宝灯 由一种花色的3张1,3张9,2,3,4,5,6,7,8,各一张组成的牌型，见同花色牌便可和。不计清一色，门前清，自摸。
	EIGHTY_EIGHT_4(0x5804, "4：大于五"),             //4：大于五 由序数为6-9的牌组成的和牌
	EIGHTY_EIGHT_5(0x5805, "5：小于五"),             //5：小于五 由序数1-4的牌组成的和牌
	EIGHTY_EIGHT_6(0x5806, "6：大七星", Arrays.asList(ONE_7, ONE_9, TWO_9, FOUR_1, TWENTY_FOUR_4, FORTY_EIGHT_3, FORTY_EIGHT_2, SIXTY_FOUR_5)),             //6：大七星 和牌为七对子，并且由东，南，西，北，中，发，白组成的和牌。不计七对，三元七对，四喜七对，全带幺，单钓将，门前清，自摸，字一色。
	EIGHTY_EIGHT_7(0x5807, "7：连七对", Arrays.asList(ONE_7, ONE_9, TWO_9, SIXTEEN_5, TWENTY_FOUR_4)),             //7：连七对 由一种花色序数相连的牌组成的7对子和牌。不计七对，单钓将，门前清，自摸，清一色。
	EIGHTY_EIGHT_8(0x5808, "8：天和", Arrays.asList(ONE_6, ONE_7, ONE_8, ONE_9, FOUR_3, FOUR_4, THIRTY_TWO_4)),             //8：天和 庄家在发完牌后就和牌称为天和，如果庄家有补花，补花之后和牌依然为天和。如果庄家发完牌之后又暗杠，不计天和。不计边张，坎张，单钓将，不求人，和绝张，自摸
	EIGHTY_EIGHT_9(0x5809, "9：地和", Arrays.asList(THIRTY_TWO_4, ONE_6, ONE_7, ONE_8, ONE_9, FOUR_3, FOUR_4)),             //9：地和 闲家摸到第一张牌就和牌，称为地和。如果闲家抓到第一张是花牌，补花之后和牌也算地和。如果闲家抓牌前有人吃，碰，杠（包括暗杠）那么不算地和。
	EIGHTY_EIGHT_10(0x580a,"10:四杠",Arrays.asList(ONE_5,FOUR_2,THIRTY_TWO_3)),					// 四杠 个杠。不计双明杠，明杠。
	;

	private String desc;

	private int value;

	private List<CoupleFanType> ignoreList;

	CoupleFanType(int value, String desc) {
		this(value, desc, null);
	}

	CoupleFanType(int value, String desc, List<CoupleFanType> ignoreList) {
		this.value = value;
		this.desc = desc;
		this.ignoreList = ignoreList;
	}

	public String getDesc() {
		return this.desc;
	}


	public int getValue() {
		return this.value;
	}

	public List<CoupleFanType> getIgnoreList() {
		return this.ignoreList;
	}

}
