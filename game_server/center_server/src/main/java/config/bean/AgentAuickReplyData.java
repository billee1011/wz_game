package config.bean;

/**
 * Created by admin on 2017/4/11.
 */
public class AgentAuickReplyData {
    private int agent_id;
    private String contents;
    private boolean selected;

    public int getAgent_id() {
        return agent_id;
    }

    public void setAgent_id(int agent_id) {
        this.agent_id = agent_id;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
