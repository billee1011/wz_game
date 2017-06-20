package conf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangfang on 2016/9/12.
 */
public abstract class BaseProvider {

    public static String CONF_PATH = null;

    public static String PROVIDER_PACKAGE = null;

    public static String PROVIDER_PATH = null;

    protected static List<BaseProvider> providerList = new ArrayList<>();

    public static void loadAll() {
        File packDirectory = new File(PROVIDER_PATH);
        for (File file : packDirectory.listFiles()) {
            String fileName = file.getName();
            try {
                Class.forName(PROVIDER_PACKAGE + "." + fileName.substring(0, fileName.indexOf(".")));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        providerList.forEach(e -> e.loadConfig());
    }

    public abstract void loadConfig();
}
