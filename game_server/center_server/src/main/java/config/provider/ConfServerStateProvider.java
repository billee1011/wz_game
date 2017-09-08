package config.provider;

import java.util.ArrayList;
import java.util.List;

import database.DataQueryResult;
import util.MapObject;
import util.Pair;

public class ConfServerStateProvider extends BaseProvider {
    private static ConfServerStateProvider inst = new ConfServerStateProvider();

    private ConfServerStateProvider() {

    }

    public static ConfServerStateProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private List<Pair<Integer, Integer>> serverState = null;

    @Override
    protected void initString() {
    }

    @Override
    public void doLoad() {
        getInfo();
    }

    private void getInfo() {
    	List<Pair<Integer, Integer>> serverState = new ArrayList<Pair<Integer,Integer>>();
    	List<MapObject> data_list = DataQueryResult.load("SELECT * FROM conf_server_state");
        for (MapObject data_info : data_list) {
        	serverState.add(new Pair<Integer, Integer>(data_info.getInt("num"), data_info.getInt("state")));
        }
        
        this.serverState = serverState;
    }

	public int getServerState(int num) {
		List<Pair<Integer, Integer>> serverState = new ArrayList<Pair<Integer,Integer>>(this.serverState);
		for(int i = serverState.size() -1 ; i > 0 ; i--){
			Pair<Integer, Integer> state  = serverState.get(i);
			if(num >= state.getLeft()){
				return state.getRight();
			}
		}
		return 1;
	}
    
}
