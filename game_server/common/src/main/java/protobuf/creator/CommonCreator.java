package protobuf.creator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ByteString;

import protobuf.Common;
import protobuf.Common.PBPair;
import util.Three;

public class CommonCreator {

	public static Common.PBGameProtocol createPBGameProtocol(int code, byte[] bytes) {
		Common.PBGameProtocol.Builder builder = Common.PBGameProtocol.newBuilder();
		builder.setResponseCode(code);
		builder.setMessage(ByteString.copyFrom(bytes));
		return builder.build();
	}

	public static Common.PBIntIntList createPBIntIntList(int key, List<Integer> value) {
		Common.PBIntIntList.Builder builder = Common.PBIntIntList.newBuilder();
		builder.setKey(key);
		value.forEach(e -> builder.addValue(e));
		return builder.build();
	}

	public static Common.PBIntIntList createPBIntIntList(int key, int value) {
		Common.PBIntIntList.Builder builder = Common.PBIntIntList.newBuilder();
		builder.setKey(key);
		builder.addValue(value);
		return builder.build();
	}

	public static Common.PBPair createPBPair(int key, int value) {
		Common.PBPair.Builder builder = Common.PBPair.newBuilder();
		builder.setKey(key);
		builder.setValue(value);
		return builder.build();
	}

	public static Common.PBPairString createPBPairString(String key, String value) {
		Common.PBPairString.Builder builder = Common.PBPairString.newBuilder();
		builder.setKey(key);
		builder.setValue(value);
		return builder.build();
	}

	public static Common.PBStringList createPBStringList(List<String> list) {
		Common.PBStringList.Builder builder = Common.PBStringList.newBuilder();
		list.forEach(e -> builder.addList(e));
		return builder.build();
	}

	public static Common.PBStringList createPBStringList(Collection<String> list) {
		Common.PBStringList.Builder builder = Common.PBStringList.newBuilder();
		list.forEach(e -> builder.addList(e));
		return builder.build();
	}

	public static Common.PBStringList createPBStringList(String... list) {
		return createPBStringList(Arrays.asList(list));
	}

	public static Common.PBInt32List createPBInt32List(List<Integer> valueList) {
		Common.PBInt32List.Builder builder = Common.PBInt32List.newBuilder();
		valueList.forEach(e -> builder.addValue(e));
		return builder.build();
	}
	
	public static List<Common.PBInt32List> createPBInt32ListList(List<List<Integer>> valueList) {
		List<Common.PBInt32List> list = new ArrayList<Common.PBInt32List>();
		for (int i = 0; i < valueList.size(); i++) {
			list.add(createPBInt32List(valueList.get(i)));
		}
		return list;
	}

	public static Common.PBTriple createPBTriple(int one, int two, int three) {
		Common.PBTriple.Builder builder = Common.PBTriple.newBuilder();
		builder.setOne(one);
		builder.setTwo(two);
		builder.setThree(three);
		return builder.build();
	}

	public static Common.PBPairListList createPBPairListList(List<Map<Integer, Integer>> mapList) {
		Common.PBPairListList.Builder builder = Common.PBPairListList.newBuilder();
		mapList.forEach(e -> builder.addList(createPBPairList(e)));
		return builder.build();
	}

	public static Common.PBPairList createPBPairList(Map<Integer, Integer> map) {
		Common.PBPairList.Builder builder = Common.PBPairList.newBuilder();
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			builder.addList(createPBPair(entry.getKey(), entry.getValue()));
		}
		return builder.build();
	}
	
	public static Common.PBPairList createPbPairList(List<PBPair> list){
		Common.PBPairList.Builder builder = Common.PBPairList.newBuilder();
		builder.addAllList(list);
		return builder.build();
	}

	public static Common.PBTripleList createPBTripleList(List<Three<Integer, Integer, Integer>> list) {
		Common.PBTripleList.Builder builder = Common.PBTripleList.newBuilder();
		list.forEach(e -> builder.addInfo(createPBTriple(e.getA(), e.getB(), e.getC())));
		return builder.build();
	}


	public static Common.PBString createPBString(String value) {
		Common.PBString.Builder builder = Common.PBString.newBuilder();
		builder.setValue(value);
		return builder.build();
	}

	public static Common.PBInt32 createPBInt32(int value) {
		Common.PBInt32.Builder builder = Common.PBInt32.newBuilder();
		builder.setValue(value);
		return builder.build();
	}
	
	public static Common.PBIntStringList createPBIntStringList(int key, String... strings) {
		Common.PBIntStringList.Builder builder = Common.PBIntStringList.newBuilder();
		builder.setKey(key);
		for (String e : strings) {
			builder.addValue(e);
		}
		return builder.build();
	}
	
	public static Common.PBIntString createPBIntString(int key, String value){
		Common.PBIntString.Builder builder = Common.PBIntString.newBuilder();
		builder.setKey(key);
		builder.setValue(value);
		return builder.build();
	}
	
	public static Common.PBInt2StringList createPBInt2StringList(Map<Integer, String> map) {
		Common.PBInt2StringList.Builder builder = Common.PBInt2StringList.newBuilder();
		for (Map.Entry<Integer, String> entry : map.entrySet()) {
			builder.addValue(createPBIntString(entry.getKey(), entry.getValue()));
		}
		return builder.build();
	}

}
