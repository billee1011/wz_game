package config.bean;

/**
 * Created by admin on 2017/4/13.
 */
public class PlayerCGConfigData {
    private int id;
    private int completion;
    private String limit_detail;
    private String forward_url;
    private int completion_type;
    private int completion_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompletion() {
        return completion;
    }

    public void setCompletion(int completion) {
        this.completion = completion;
    }

    public String getForward_url() {
        return forward_url;
    }

    public void setForward_url(String forward_url) {
        this.forward_url = forward_url;
    }

    public int getCompletion_type() {
        return completion_type;
    }

    public void setCompletion_type(int completion_type) {
        this.completion_type = completion_type;
    }

    public int getCompletion_time() {
        return completion_time;
    }

    public void setCompletion_time(int completion_time) {
        this.completion_time = completion_time;
    }

    public String getLimit_detail() {
        return limit_detail;
    }

    public void setLimit_detail(String limit_detail) {
        this.limit_detail = limit_detail;
    }
}
