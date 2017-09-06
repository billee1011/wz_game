package define;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

public enum Icon {
	// 男
	HEAD_1(1, "icon/head/head_1.png"),
	HEAD_2(1, "icon/head/head_2.png"),
	HEAD_3(1, "icon/head/head_3.png"),
	HEAD_4(1, "icon/head/head_4.png"),
	HEAD_5(1, "icon/head/head_5.png"),
	// 女
	HEAD_6(2, "icon/head/head_6.png"),
	HEAD_7(2, "icon/head/head_7.png"),
	HEAD_8(2, "icon/head/head_8.png"),
	HEAD_9(2, "icon/head/head_9.png"),
	HEAD_10(2, "icon/head/head_10.png"),;
	
	private int genderValue;
	
	private String iconString;
	
	Icon(int genderValue, String iconString) {
		this.genderValue = genderValue;
		this.iconString = iconString;
	}
	
	public int getGenderValue() {
		return genderValue;
	}

	public String getIconString() {
		return iconString;
	}

	public static String randomIconString(int value) {
		List<Icon> iconList = new ArrayList<Icon>();
		for (Icon icon : values()) {
			if (icon.getGenderValue() == value) {
				iconList.add(icon);
			}
		}
		return iconList.get(RandomUtils.nextInt(iconList.size())).getIconString();
	}

}
