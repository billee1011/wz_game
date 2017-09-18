package reader;

import annotation.EnumField;
import annotation.ListDesc;
import bean.EItemQuality;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import net.sf.json.JSONArray;
import util.ConvertUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executors;

public class ExcelReader {
	static String USER_DIR = System.getProperty("user.dir") + File.separator;

	static String excelPath = USER_DIR + ".." + File.separator + "sg_excel" + File.separator;

	public static void main(String[] args) throws Exception {

//		if (args.length == 0) {
//			System.out.println("wo kao    error  return a a a a ");
//		}
//		String configPath = args[0];
//
//		System.out.println("the config path is :" + configPath + File.separator);
//		BufferedReader reader = null;
//		try {
//			reader = new BufferedReader(new InputStreamReader(new FileInputStream(USER_DIR + "build.config")));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		List<String> stringList = new ArrayList<String>();
//		String line = "";
//		while ((line = reader.readLine()) != null) {
//			if (line.startsWith("//")) {
//				continue;
//			}
//			String result = new String(line.getBytes(), "utf-8");
//			System.out.println("the result is" + result);
//
//			stringList.add(result);
//		}
//		new MainFrame(configPath, stringList);

		readExcel("Hero.xls");
		readExcel("Equip.xls");
		readExcel("skill.xls");
	}

	private static void readExcel(String fileName) {
		readExcel(excelPath + fileName, 1);
	}

	public static List<Map<String, String>> readExcelAsMapList(Sheet sheet, int row) throws Exception {
		int rowsCounts = sheet.getRows();
		int colCount = sheet.getColumns();
		String[] keys = new String[colCount];
		for (int i = 0; i < colCount; i++) {
			keys[i] = sheet.getCell(i, 0).getContents().split("\n")[0].split("\\(")[0].trim();
		}
		List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
		for (int i = (row + 1); i < rowsCounts; i++) {
			Map<String, String> map = new HashMap<String, String>();
			for (int j = 0; j < colCount; j++) {
				map.put(keys[j], sheet.getCell(j, i).getContents().trim());
			}
			int nullStrNum = 0;
			Iterator<String> iterStr = map.keySet().iterator();
			while (iterStr.hasNext()) {
				if (map.get(iterStr.next()).equals("")) {
					nullStrNum++;
				}
			}
			if (nullStrNum >= 10) {
				continue;
			}
			mapList.add(map);
		}
		return mapList;
	}

	public static void readExcel(String excelPath, int row) {
		Workbook workbook = null;
		try {
			workbook = Workbook.getWorkbook(new File(excelPath));
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < workbook.getSheets().length; i++) {
			String sheetName = workbook.getSheet(i).getName();
			if (sheetName.startsWith("Sheet")) {
				continue;
			}
			List<Map<String, String>> mapList = null;
			try {
				mapList = readExcelAsMapList(workbook.getSheet(i), row);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				writeClientConfig(mapList, sheetName);
				writeServerConfig(mapList, sheetName);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void writeClientConfig(List<Map<String, String>> mapList, String className) throws Exception {
		Iterator<Map<String, String>> iter = mapList.iterator();
		List<Object> monsterList = new ArrayList<Object>();
		while (iter.hasNext()) {
			Object monster = Class.forName("bean." + className + "Client").newInstance();
			Field[] fileds = monster.getClass().getDeclaredFields();
			Map<String, String> gaga = iter.next();
			Iterator<String> iterSec = gaga.keySet().iterator();
			while (iterSec.hasNext()) {
				String key = iterSec.next();
				for (int i = 0; i < fileds.length; i++) {
					if (fileds[i].getName().equals(key)) {
						fileds[i].setAccessible(true);
						setFieldValue(fileds[i], monster, gaga.get(key));
					}
				}
			}
			monsterList.add(monster);
		}
		String content = JSONArray.fromObject(monsterList).toString();
		;
		writeToFile(false, content, className);
	}

	public static <T> String getPrimitiveStrList(List list, Class<T> classType) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		int size = list.size();
		for (int i = 0; i < size; i++) {
			if (i != size - 1) {
				if (classType.getName().equals("java.lang.String")) {
					builder.append("\"");
					builder.append(list.get(i));
					builder.append("\"");
				} else {
					builder.append(list.get(i));
				}
				builder.append(",");
			}
		}
		builder.append("}");
		return builder.toString();
	}

	static int deep = 0;

	public static <T> String writeLua(List<T> list, boolean sub) {
		if (list.size() == 0) {
			return "";
		}
		Class<?> classType = list.get(0).getClass();
		if (classType.isPrimitive() || classType.getName().equals("java.lang.String")) {
			return getPrimitiveStrList(list, list.get(0).getClass());
		}
		StringBuilder buff = new StringBuilder();
		if (!sub) {
			buff.append("return {\r\n");
		} else {
			buff.append("{");
		}
		int size = list.size();
		for (int i = 0; i < size; i++) {
			Object e = list.get(i);
			try {
				if (e instanceof List) {
					buff.append(writeLua((List) e, true));
				} else {
					buff.append(writeLuaTable(e, sub));
				}
				if (i != size - 1) {
					buff.append(",");
				}
				if (!sub) {
					buff.append("\n");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		if (!sub) {
			buff.append("}");
		} else {
			buff.append("}");
		}
		deep = 0;
		return buff.toString();

	}


	public static <T> String writeLuaTable(T t, boolean sub) throws Exception {
		if (t == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		Field[] fields = t.getClass().getDeclaredFields();
		int length = fields.length;
		if (sub) {
			builder.append("{");
		}
		for (int i = 0; i < length; i++) {
			fields[i].setAccessible(true);
			if (fields[i].getName() == "id" && !sub) {
				builder.append("    [" + fields[i].get(t) + "] = { ");
			} else {
				if (sub) {
					builder.append("[\"" + fields[i].getName() + "\"]");
				} else {
					builder.append("[\"" + fields[i].getName() + "\"]");
				}
				String fieldName = fields[i].getType().getName();
				if (fieldName.equals("java.lang.String")) {
					builder.append(" = \"" + fields[i].get(t) + "\"");
				} else if (fieldName.equals("java.util.List")) {
					builder.append(" = " + writeLua((List) (fields[i].get(t)), true));
				} else if (fieldName.equals("int")) {
					builder.append(" = " + fields[i].get(t));
				} else if (fieldName.equals("long")) {
					builder.append(" = " + fields[i].get(t));
				} else {
					builder.append(" = " + writeLuaTable(fields[i].get(t), true));
				}
				builder.append(",");
			}
		}

		builder.append("}");

		return builder.toString();
	}

	public static void writeServerConfig(List<Map<String, String>> mapList, String className) throws Exception {
		Iterator<Map<String, String>> iter = mapList.iterator();
		List<Object> monsterList = new ArrayList<Object>();
		while (iter.hasNext()) {
			Object monster = Class.forName("bean." + className).newInstance();
			Field[] fileds = monster.getClass().getDeclaredFields();
			Map<String, String> gaga = iter.next();
			Iterator<String> iterSec = gaga.keySet().iterator();
			while (iterSec.hasNext()) {
				String key = iterSec.next();
				for (int i = 0; i < fileds.length; i++) {
					if (fileds[i].getName().equals(key)) {
						fileds[i].setAccessible(true);
						setFieldValue(fileds[i], monster, gaga.get(key));
					}
				}
			}
			monsterList.add(monster);
		}
		String content = JSONArray.fromObject(monsterList).toString();
		writeToFile(true, content, className);

	}

	public static void setFieldValue(Field field, Object object, String value) throws Exception {
		System.out.println("the result filed name is " + field.getType().toString());
		Class<?> classType = field.getType();
		String fileTypeName = classType.getName();
		if (classType.isEnum()) {
			Method method = classType.getMethod("valueOf", String.class);
			try {
				field.set(object, method.invoke(classType, value));
			} catch (Exception e) {
				field.set(object, null);
			}
		} else {
			if (fileTypeName.equals("int")) {
				EnumField enumAnnotation = field.getAnnotation(EnumField.class);
				if (enumAnnotation == null) {
					field.setInt(object, ConvertUtil.safe2Int(value));
				} else {
					String enumName = enumAnnotation.value();
					Class<?> enumType = Class.forName("bean." + enumName);
					Method method = enumType.getMethod("valueOf", String.class);
					Method method1 = enumType.getMethod("getValue");
					Object enumValue = method.invoke(enumType, value);
					Object intValue = method1.invoke(enumValue);
					field.setInt(object, (int) intValue);
				}
			} else if (fileTypeName.equals("java.util.List")) {
				setArrayValueToObject(field, object, value);
			} else if (fileTypeName.equals("long")) {
				field.setLong(object, ConvertUtil.safe2Long(value));
			} else if (fileTypeName.equals("float")) {
				field.setFloat(object, ConvertUtil.safe2Float(value));
			} else if (fileTypeName.equals("java.lang.String")) {
				field.set(object, value);
			} else {
				field.set(object, getStructData(value, field.getType().getName(), true));
			}
		}
	}

	static void setArrayValueToObject(Field field, Object obj, String value) throws Exception {
		ListDesc annotation = field.getAnnotation(ListDesc.class);
		if (annotation == null) {
			throw new RuntimeException(" error  type in list desc  ");
		}
		List list = null;
		if (value.startsWith("[")) {
			list = getArray2List(value, annotation.value());
		} else {
			list = getArrayList(value, annotation.value());
		}
		field.set(obj, list);
	}

	static List getArrayList(String value, String subType) {
		List result = new ArrayList();
		String[] list = value.split(",");
		int length = list.length;
		for (int i = 0; i < length; i++) {
			String s = list[i];
			switch (subType) {
				case "int":
					result.add(ConvertUtil.safe2Int(s));
					continue;
				case "long":
					result.add(ConvertUtil.safe2Long(s));
					continue;
				case "float":
					result.add(ConvertUtil.safe2Float(s));
					continue;
				case "string":
					result.add(s);
					continue;
				default:
					Class<?> classType;
					try {
						classType = Class.forName("bean." + subType);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						return null;
					}
					if (classType.isEnum()) {
						try {
							Method method = classType.getMethod("valueOf", String.class);
							result.add(method.invoke(classType, list[i]));
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					} else {
						result.add(getStructData(s, subType));
					}
					continue;
					//这个时候当做结构体
			}
		}
		return result;
	}

	static String[] getStructStringList(String s) {
		String subString = s.substring(s.indexOf("{") + 1, s.indexOf("}"));
		return subString.split(":");
	}

	static Object getStructData(String s, String type, boolean single) {
		String finalClassName = type.startsWith("bean") ? type : "bean." + type;
		try {
			Object obj = Class.forName(finalClassName).newInstance();
			Field[] fileds = obj.getClass().getDeclaredFields();
			String[] strs = single ? s.split(":") : getStructStringList(s);
			if (fileds.length != strs.length) {
				return null;
			}
			for (int i = 0, length = fileds.length; i < length; i++) {
				try {
					fileds[i].setAccessible(true);
					setFieldValue(fileds[i], obj, strs[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return obj;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	static Object getStructData(String s, String type) {
		return getStructData(s, type, false);
	}

	static List getArray2List(String value, String subType) {
		List<String> stringList = new ArrayList<>();
		while (value != null) {
			int beginIndex = value.indexOf("[");
			int endIndex = value.indexOf("]");
			if (beginIndex == -1 || endIndex == -1) {
				value = null;
				continue;
			}
			stringList.add(value.substring(beginIndex + 1, endIndex));
			value = value.substring(endIndex + 1, value.length());
		}
		List list = new ArrayList();
		for (int i = 0; i < stringList.size(); i++) {
			list.add(getArrayList(stringList.get(i), subType));
		}
		return list;
	}


	public static void writeToFile(boolean server, String content, String className) throws Exception {
		String path = USER_DIR + ".." + File.separator + "conf_file" + File.separator;
		if (server) {
			path += "server" + File.separator + className + ".json";
		} else {
			path += "client" + File.separator + className + ".json";
		}
		File resultFile = new File(path);
		if (resultFile.exists() == false) {
			resultFile.createNewFile();
		}
		OutputStream out = new FileOutputStream(resultFile);
		out.write(content.getBytes("utf-8"));
		out.close();
	}


}

