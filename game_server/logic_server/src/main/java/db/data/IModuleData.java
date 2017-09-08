package db.data;


import util.MapObject;

import java.sql.SQLException;

public interface IModuleData {

    void load() throws SQLException ;
    
    void save() throws SQLException ;
    
    void update(MapObject moduleData);

    MapObject getData();
    
    boolean checkSame(MapObject newData);


}
