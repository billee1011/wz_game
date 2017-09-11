package generate;

import util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileGenerate {
	public static void generateAllFile() {
		generateJavaServerFile();
		generateJavaClientFile();
		geneJavaEnumFile();
		geneCsEnumFile();
	}


	static String firstToUp(String src) {
		String result = src;
		result.toCharArray()[0] = Character.toUpperCase(result.charAt(0));
		return result;
	}

	static void geneJavaEnumFile() {
		PackConfigManager.getInst().getEnum_list().forEach(FileGenerate::geneOneJavaEnum);
	}

	static void geneCsEnumFile() {
		PackConfigManager.getInst().getEnum_list().forEach(FileGenerate::geneOneCsEnum);
	}

	static void generateJavaServerFile() {
		PackConfigManager.getInst().getConfigList().forEach(e -> geneOneJavaFile(e, true));
	}

	static void generateJavaClientFile() {
		PackConfigManager.getInst().getConfigList().forEach(e -> geneOneJavaFile(e, false));
		PackConfigManager.getInst().getConfigList().forEach(FileGenerate::geneCsFile);
	}


	static List<String> getNeedImportPackage(FieldConfig[] fieldList) {
		List<String> result = null;
		for (FieldConfig conf : fieldList) {
			if (conf.xiushifu.startsWith("array")) {
				result = new ArrayList<>();
				result.add("java.util.List");
				result.add("annotation.ListDesc");
			}
		}
		return result;
	}

	static String getAnnotation(String modify) {
		return "\t@ListDesc(\"" + modify.split("_")[1] + "\")\n";
	}


	static void geneOneCsEnum(EnumConfig config) {
		StringBuilder builder = new StringBuilder(128);
		builder.append("public enum " + config.className);
		builder.append("{\n");
		List<Pair<String, Integer>> memberList = config.memberList;
		for (int i = 0, size = memberList.size(); i < size; i++) {
			builder.append("	");
			builder.append(memberList.get(i).getLeft());
			builder.append(" = ");
			builder.append(memberList.get(i).getRight());
			builder.append(",\n");
		}
		builder.append("}");
		try {
			writeToFile(builder.toString(), config.className, FileType.CS_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void geneOneJavaEnum(EnumConfig config) {
		StringBuilder builder = new StringBuilder(128);
		builder.append("package bean;\n\n");
		builder.append("public enum " + config.className);
		builder.append("{\n");
		List<Pair<String, Integer>> memberList = config.memberList;
		for (int i = 0, size = memberList.size(); i < size; i++) {
			builder.append("	");
			builder.append(memberList.get(i).getLeft());
			builder.append("(");
			builder.append(memberList.get(i).getRight());
			builder.append(")");
			if (i == size - 1) {
				builder.append(";\n\n");
			} else {
				builder.append(",\n");
			}
		}
		builder.append("	");
		builder.append(config.className);
		builder.append("(int value){\n");
		builder.append("		this.value = value;\n");
		builder.append("	}\n\n");
		builder.append("	private int value;\n\n");
		builder.append("	public int getValue(){\n");
		builder.append("		return this.value;\n");
		builder.append("	}\n\n");
		builder.append("	public static ");
		builder.append(config.className);
		builder.append(" getByValue(int value){\n");
		builder.append("		for(");
		builder.append(config.className);
		builder.append(" type : values()){\n");
		builder.append("			if(type.getValue() == value){\n");
		builder.append("				return type;\n");
		builder.append("			}\n");
		builder.append("		}\n");
		builder.append("		return null;\n");
		builder.append("	}\n\n");
		builder.append("}");
		try {
			writeToFile(builder.toString(), config.className, FileType.JAVA_SERVER_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	static void geneCsFile(ClassConfig config) {
		StringBuilder builder = new StringBuilder();
		builder.append("using System.Collections.Generic;\n");
		builder.append("using System.Text;\n\n");
		builder.append("public class " + config.className);
		builder.append("{\n");
		FieldConfig[] fieldList = config.clientFieldsList;
		for (int i = 0; i < fieldList.length; i++) {
			String filedModify = fieldList[i].xiushifu;
			if (filedModify.startsWith("array")) {
				filedModify = getFinalCsTypeString(filedModify);
			}
			if(filedModify.equals("String")){
				filedModify = "string";
			}
			builder.append("\tprivate " + filedModify + " " + fieldList[i].fieldName + "; " + "\n\n");
		}
		for (int i = 0; i < fieldList.length; i++) {
			String filedModify = fieldList[i].xiushifu;
			if (filedModify.startsWith("array")) {
				filedModify = getFinalCsTypeString(filedModify);
			}
			if(filedModify.equals("String")){
				filedModify = "string";
			}
			builder.append("\tpublic " + filedModify + " get" + oneWordToUpper(fieldList[i].fieldName) + "(){\n");
			builder.append("\t\treturn " + fieldList[i].fieldName + ";\n");
			builder.append("\t}\n\n");
			builder.append("\tpublic void set" + oneWordToUpper(fieldList[i].fieldName) + "(" + filedModify + " " + fieldList[i].fieldName + "){\n");
			builder.append("\t\tthis." + fieldList[i].fieldName + " = " + fieldList[i].fieldName + ";\n");
			builder.append("\t}\n\n");
		}
		//给每个对象加上toString方法可以让一切更容易调试
		builder.append("	public string toString(){\n");
		builder.append("		StringBuilder builder = new StringBuilder();\n");
		for (int i = 0; i < fieldList.length; i++) {
			builder.append("		builder.Append(\"");
			builder.append(fieldList[i].fieldName);
			builder.append("\");\n");
			builder.append("		builder.Append(\":\");\n");
			builder.append("		builder.Append(");
			builder.append(fieldList[i].fieldName);
			builder.append(");\n");
			if (i != fieldList.length - 1) {
				builder.append("		builder.Append(\",\");\n");
			}
		}
		builder.append("		return builder.ToString();\n");
		builder.append("	}\n");
		builder.append("}");
		try {
			writeToFile(builder.toString(), config.className, FileType.CS_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void geneOneJavaFile(ClassConfig config, boolean server) {
		StringBuilder builder = new StringBuilder();
		builder.append("package bean;\n\n");
		List<String> importPacks = server ? getNeedImportPackage(config.serverFieldsList) : getNeedImportPackage(config.clientFieldsList);
		if (importPacks != null) {
			for (String pack : importPacks) {
				builder.append("import " + pack + ";\n");
			}
		}
		builder.append("public class " + config.className);
		if (!server) {
			builder.append("Client");
		}
		builder.append("{\n");
		FieldConfig[] fieldList = server ? config.serverFieldsList : config.clientFieldsList;
		for (int i = 0; i < fieldList.length; i++) {
			String filedModify = fieldList[i].xiushifu;
			if (filedModify.startsWith("array")) {
				builder.append(getAnnotation(filedModify));
				filedModify = getFinalJavaTypeString(filedModify);
			} else if (filedModify.equals("string")) {
				filedModify = "String";
			}
			builder.append("\tprivate " + filedModify + " " + fieldList[i].fieldName + "; " + "\n\n");
		}
		for (int i = 0; i < fieldList.length; i++) {
			String filedModify = fieldList[i].xiushifu;
			if (filedModify.startsWith("array")) {
				filedModify = getFinalJavaTypeString(filedModify);
			} else if (filedModify.equals("string")) {
				filedModify = "String";
			}
			builder.append("\tpublic " + filedModify + " get" + oneWordToUpper(fieldList[i].fieldName) + "(){\n");
			builder.append("\t\treturn " + fieldList[i].fieldName + ";\n");
			builder.append("\t}\n\n");
			builder.append("\tpublic void set" + oneWordToUpper(fieldList[i].fieldName) + "(" + filedModify + " " + fieldList[i].fieldName + "){\n");
			builder.append("\t\tthis." + fieldList[i].fieldName + " = " + fieldList[i].fieldName + ";\n");
			builder.append("\t}\n\n");
		}
		//给每个对象加上toString方法可以让一切更容易调试
		builder.append("	@Override\n");
		builder.append("	public String toString(){\n");
		builder.append("		StringBuilder builder = new StringBuilder();\n");
		for (int i = 0; i < fieldList.length; i++) {
			builder.append("		builder.append(\"");
			builder.append(fieldList[i].fieldName);
			builder.append("\");\n");
			builder.append("		builder.append(\":\");\n");
			builder.append("		builder.append(");
			builder.append(fieldList[i].fieldName);
			builder.append(");\n");
			if (i != fieldList.length - 1) {
				builder.append("		builder.append(\",\");\n");
			}
		}
		builder.append("		return builder.toString();\n");
		builder.append("	}\n");
		builder.append("}");
		try {
			writeToFile(builder.toString(), config.className, server ? FileType.JAVA_SERVER_FILE : FileType.JAVA_CLIENT_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String getFinalTypeInJavaArray(String original) {
		switch (original) {
			case "int":
				return "Integer";
			case "bool":
				return "Boolean";
			case "float":
				return "Float";
			case "byte":
				return "Byte";
			case "long":
				return "Long";
			case "string":
				return "String";
			default:
				return original;
		}
	}

	static String getFinalCsTypeString(String original) {
		String inputType = original.substring(original.lastIndexOf("_") + 1, original.length());
		String finalArrayType = inputType;
		if (original.startsWith("array2")) {
			return String.format("List<List<%s>>", finalArrayType);
		} else if (original.startsWith("array")) {
			return String.format("List<%s>", finalArrayType);
		} else {
			throw new RuntimeException(" the array type is not defined ");
		}
	}

	static String getFinalJavaTypeString(String original) {
		String inputType = original.substring(original.lastIndexOf("_") + 1, original.length());
		String finalArrayType = getFinalTypeInJavaArray(inputType);
		if (original.startsWith("array2")) {
			return String.format("List<List<%s>>", finalArrayType);
		} else if (original.startsWith("array")) {
			return String.format("List<%s>", finalArrayType);
		} else {
			throw new RuntimeException(" the array type is not defined ");
		}
	}

	static void writeToFile(String content, String className, FileType fileType) throws Exception {
		String path = getPath(fileType);
		File fileDir = new File(path);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		String postFix = getPostFixByType(fileType);
		File file = new File(path + className + postFix);
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		out.write(content.getBytes());
		out.close();
	}

	private static String getPath(FileType type) {
		if (type == FileType.JAVA_SERVER_FILE) {
			return FilePath.SERVER_JAVA_CONFIG_BEAN;
		} else if (type == FileType.JAVA_CLIENT_FILE) {
			return FilePath.CLIENT_JAVA_CONFIG_BEAN;
		} else {
			return FilePath.CLIENT_CS_CONFIG_BEAN;
		}
	}

	static String getPostFixByType(FileType type) {
		if (type == FileType.JAVA_CLIENT_FILE) {
			return "Client.java";
		}
		if (type == FileType.CS_FILE) {
			return ".cs";
		}

		return ".java";
	}

	public static void main(String[] args) throws Exception {
		PackConfigManager.getInst().load();
		generateAllFile();
	}

	static String oneWordToUpper(String str) {
		String strOne = str.substring(1);
		String strTwo = str.substring(0, 1);
		String resultStr = strTwo.toUpperCase() + strOne;
		return resultStr;
	}

	private enum FileType {
		CS_FILE,
		JAVA_SERVER_FILE,
		JAVA_CLIENT_FILE;
	}
}