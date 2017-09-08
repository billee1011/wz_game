package config.bean;

import java.io.Serializable;

import util.MapObject;

/**
 * Created by admin on 2017/4/2.
 */
public class TransferData implements Serializable{
    private int id;
    private int player_out_id;
    private String player_in_name;
    private int player_in_id;
    private long amount;
    private int time;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlayer_out_id() {
        return player_out_id;
    }

    public void setPlayer_out_id(int player_out_id) {
        this.player_out_id = player_out_id;
    }

    public String getPlayer_in_name() {
        return player_in_name;
    }

    public void setPlayer_in_name(String player_in_name) {
        this.player_in_name = player_in_name;
    }

    public int getPlayer_in_id() {
        return player_in_id;
    }

    public void setPlayer_in_id(int player_in_id) {
        this.player_in_id = player_in_id;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public static TransferData createTransferData(MapObject data) {
        TransferData transfer_data = new TransferData();
        transfer_data.setId(data.getInt("id"));
        transfer_data.setPlayer_out_id(data.getInt("player_out_id"));
        transfer_data.setPlayer_in_id(data.getInt("player_in_id"));
        transfer_data.setPlayer_in_name(data.getString("player_in_name"));
        transfer_data.setAmount(data.getLong("amount"));
        transfer_data.setType(data.getInt("type"));
        transfer_data.setTime(data.getInt("time"));
        return transfer_data;
    }

    public static TransferData createTransferData(int id, int player_out_id, int player_in_id, String player_in_name, int amount, int type, int time) {
        TransferData transfer_data = new TransferData();
        transfer_data.setId(id);
        transfer_data.setPlayer_out_id(player_out_id);
        transfer_data.setPlayer_in_id(player_in_id);
        transfer_data.setPlayer_in_name(player_in_name);
        transfer_data.setAmount(amount);
        transfer_data.setType(type);
        transfer_data.setTime(time);
        return transfer_data;
    }
}
