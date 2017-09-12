package util;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackConfigManager {
	private static Logger logger = LoggerFactory.getLogger(PackConfigManager.class);
	private static PackConfigManager config = new PackConfigManager();

	private List<ClassConfig> config_list = new ArrayList();

	private List<EnumConfig> enum_list = new ArrayList<>();

	public static PackConfigManager getInst() {
		return config;
	}


	public boolean isEnum(String className) {
		return enum_list.stream().anyMatch( e -> e.className.equals(className));
	}

	private HSSFWorkbook createHSSFWorkbook(File file) {
		HSSFWorkbook workbook;
		try {
			workbook = new HSSFWorkbook(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			logger.error("{}", e);
			return null;
		} catch (IOException e) {
			logger.error("{}", e);
			return null;
		}
		return workbook;
	}

	private void loadFile(HSSFWorkbook workbook) {
		int sheetSize = workbook.getNumberOfSheets();
		for (int i = 0; i < sheetSize; i++) {
			HSSFSheet sheet = workbook.getSheetAt(i);
			if (sheet.getSheetName().startsWith("Sheet")) {
				continue;
			}
			loadNormalSheet(sheet);
		}
	}

	private void loadEnumFile(HSSFWorkbook workbook) {
		int sheetSize = workbook.getNumberOfSheets();
		for (int i = 0; i < sheetSize; i++) {
			HSSFSheet sheet = workbook.getSheetAt(i);
			if (sheet.getSheetName().startsWith("Sheet")) {
				continue;
			}
			loadEnumSheet(sheet);
		}
	}

	private void loadBaseFile(HSSFWorkbook workbook) {
		int sheetSize = workbook.getNumberOfSheets();
		for (int i = 0; i < sheetSize; i++) {
			HSSFSheet sheet = workbook.getSheetAt(i);
			if (sheet.getSheetName().startsWith("Sheet")) {
				continue;
			}
			loadBaseSheet(sheet);
		}
	}

	private void loadEnumSheet(HSSFSheet sheet) {
		int rowNum = sheet.getPhysicalNumberOfRows();
		EnumConfig config = new EnumConfig();
		config.className = sheet.getSheetName();
		for (int i = 0; i < rowNum; i++) {
			HSSFRow row = sheet.getRow(i);
			String memberName = row.getCell(0).getStringCellValue();
			int value = (int) row.getCell(1).getNumericCellValue();
			config.memberList.add(new Pair<>(memberName, value));
		}
		enum_list.add(config);
	}

	private void loadBaseSheet(HSSFSheet sheet) {
		int rowNum = sheet.getPhysicalNumberOfRows();
		ClassConfig config = new ClassConfig();
		config.className = sheet.getSheetName();
		config.clientFieldsList = new FieldConfig[rowNum];
		config.serverFieldsList = new FieldConfig[rowNum];
		for (int i = 0; i < rowNum; i++) {
			HSSFRow row = sheet.getRow(i);
			String fieldName = row.getCell(0).getStringCellValue();
			String modifier = row.getCell(1).getStringCellValue();
			FieldConfig fieConf = new FieldConfig();
			fieConf.xiushifu = modifier;
			fieConf.fieldName = fieldName;
			config.clientFieldsList[i] = fieConf;
			config.serverFieldsList[i] = fieConf;
		}
		config_list.add(config);
	}

	private void loadNormalSheet(HSSFSheet sheet) {
		Map<Integer, String> fieldMap = new HashMap<>();
		HSSFRow filedNameRow = sheet.getRow(0);
		int columnCount = filedNameRow.getPhysicalNumberOfCells();
		for (int i = 0; i < columnCount; i++) {
			fieldMap.put(i, filedNameRow.getCell(i).getStringCellValue());
		}
		HSSFRow secondRow = sheet.getRow(1);
		int secondColumnCount = secondRow.getPhysicalNumberOfCells();
//		if (columnCount != secondColumnCount) {
//			throw new RuntimeException(" the filed row is not equal second row ");
//		}
		List<FieldConfig> clientList = new ArrayList<>();
		List<FieldConfig> serverList = new ArrayList<>();
		for (int i = 0; i < secondColumnCount; i++) {
			String modifyName = secondRow.getCell(i).getStringCellValue();
			System.out.println("the total col num is " + secondColumnCount + "the column is " + i + "and the modify name is " + modifyName);
			FieldConfig fieldConfig = new FieldConfig();
			fieldConfig.fieldName = fieldMap.get(i);
			fieldConfig.xiushifu = getModifyName(modifyName);
			if (modifyName.endsWith("_s")) {
				serverList.add(fieldConfig);
			} else if (modifyName.endsWith("_c")) {
				clientList.add(fieldConfig);
			} else {
				serverList.add(fieldConfig);
				clientList.add(fieldConfig);
			}
		}
		ClassConfig config = new ClassConfig();
		config.className = sheet.getSheetName();
		config.serverFieldsList = new FieldConfig[serverList.size()];
		config.clientFieldsList = new FieldConfig[clientList.size()];
		serverList.toArray(config.serverFieldsList);
		clientList.toArray(config.clientFieldsList);
		config_list.add(config);
	}

	private String getModifyName(String name) {
		if (name.endsWith("_s") || name.endsWith("_c")) {
			return name.substring(0, name.lastIndexOf("_"));
		} else {
			return name;
		}
	}

	public void load() throws Exception {
		String userPath = System.getProperty("user.dir");
		System.out.println("the user path is " + userPath);
		File excelDir = new File(userPath + File.separator + ".." + File.separator + "sg_excel");
		if (!excelDir.exists()) {
			throw new RuntimeException(" the excel config directory is not exist ");
		}
		System.out.println("begin load excel file list");
		File[] files = excelDir.listFiles();
		for (File file : files) {
			HSSFWorkbook workbook = createHSSFWorkbook(file);
			if (workbook == null) {
				logger.error(" create excel file failed ");
				continue;
			}
			System.out.println("begin load file  " + file.getName());
			if (file.getName().startsWith("base")) {
				System.out.println(" load base file ");
				loadBaseFile(workbook);
			} else if (file.getName().startsWith("enum")) {
				System.out.println(" load enum file ");
				loadEnumFile(workbook);
			} else {
				System.out.println("load other file ");
				loadFile(workbook);
			}
		}
	}

	public List<ClassConfig> getConfigList() {
		System.out.println("the config size is" + this.config_list.size());
		return this.config_list;
	}

	public List<EnumConfig> getEnum_list() {
		System.out.println("the enum config size is" + this.enum_list.size());
		return enum_list;
	}

	public static void main(String[] args) throws Exception {
		getInst().load();
	}
}