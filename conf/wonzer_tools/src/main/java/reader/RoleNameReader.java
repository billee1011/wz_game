package reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;

public class RoleNameReader {
	
	public static void main(String[] args) throws Exception
	{
		Workbook workbook = Workbook.getWorkbook(new File(FilePath.DESIGN_CONF_PATH + "CharNames_人名.xls"));
		Sheet sheet = workbook.getSheet(0);
		List<String> stringList = new ArrayList<String>();
		int colCount = sheet.getColumns();
		int rowCount = sheet.getRows();
		for(int i = 3; i < rowCount ; i++){
			String nameList = "";
			for( int j = 0 ; j < colCount ; j++){
				if( j == 2){
					nameList += sheet.getCell(j , i).getContents().trim();
				}
				else{
					nameList += sheet.getCell(j,i).getContents().trim();
					nameList += ",";
				}
			}
			stringList.add(nameList);
		}
		
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream("D:" +File.separator + "charName.txt")));
		
		for( int i = 0 ; i < stringList.size() ; i++){
			writer.write(stringList.get(i) + "\n");
		}
	}
}	
