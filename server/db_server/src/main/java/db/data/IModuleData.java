package db.data;


import util.ASObject;

import java.sql.SQLException;

public interface IModuleData {

    void load() throws SQLException ;
    
    void save() throws SQLException ;
    
    void update(ASObject moduleData);

    ASObject getData();
    
    boolean checkSame(ASObject newData);


}
