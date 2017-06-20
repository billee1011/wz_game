package reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PackConfigManager {
	
	private static PackConfigManager config = new PackConfigManager();
	
	
	private List<PackConfig> config_list = new ArrayList<PackConfig>();
	
	private PackConfigManager(){
		
	}
	
	public static PackConfigManager getInst(){
		return config;
	}
	
	
	public void load() throws Exception{
		InputStream in = new FileInputStream(FilePath.PACK_CONFIG_PATH);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line1 = "";
		String line2 = "";
		String line3 = "";
		while((line1 = reader.readLine()) != null && (line2 = reader.readLine()) != null && (line3 = reader.readLine()) != null){
			PackConfig config = new PackConfig();
			System.out.println(line1);
			if(line1.trim().equals("")){
				break;
			}
			config.className = line1.trim();
			String line2Result = line2.trim();
			String line3Result = line3.trim();
			config.clientFieldsList = line2Result.split(",");
			config.serverFieldsList = line3Result.split(",");
			config_list.add(config);
		}
		System.out.println("the config length " + config_list.size());
		
	}
	
	public static void main(String[] args) throws Exception{
		PackConfigManager.getInst().load();
	}
}

class PackConfig{
	public String className = "";
	
	public String[] clientFieldsList;
	
	public String[] serverFieldsList;;
	
}
