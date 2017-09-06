package database;

public class DBValue {

    private Object value;
    private int type;

    public DBValue(Object value, int type) {
        this.setValue(value);
        this.setType(type);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
