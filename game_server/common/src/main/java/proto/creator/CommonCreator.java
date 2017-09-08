package proto.creator;

import proto.Common;

import java.util.List;

public class CommonCreator {


	public static Common.PBStringList stringList(List<String> list) {
		Common.PBStringList.Builder builder = Common.PBStringList.newBuilder();
		list.forEach(e -> builder.addList(e));
		return builder.build();
	}

	public static Common.PBStringList stringList(String... strs) {
		Common.PBStringList.Builder builder = Common.PBStringList.newBuilder();
		for (int i = 0, length = strs.length; i < length; i++) {
			builder.addList(strs[i]);
		}
		return builder.build();
	}

	public static Common.PBInt32 int32(int value) {
		Common.PBInt32.Builder builder = Common.PBInt32.newBuilder();
		builder.setValue(value);
		return builder.build();
	}

}
