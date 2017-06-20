package bean;

/**
 * Created by Administrator on 2017/1/20.
 */
public class ClientData <T>{

    private T list;

    public ClientData(T t){
        this.list = t;
    }

    public T getList() {
        return list;
    }

    public void setList(T list) {
        this.list = list;
    }
}
