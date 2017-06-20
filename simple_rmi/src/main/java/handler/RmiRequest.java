package handler;

import io.netty.buffer.ByteBuf;
import util.DataUtil;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Map;

/**
 * Created by think on 2017/4/17.
 */
public class RmiRequest extends AbstractRmiMessage {
	private String interfaceName;

	@Override
	public int getSequenceId() {
		return 0;
	}

	private String methodName;

	private List<Pair<String, Object>> paramsMap;


	public RmiRequest() {
	}

	public RmiRequest(String interfaceName, String methodName, List<Pair<String, Object>> paramsMap) {
		super(1);
		this.interfaceName = interfaceName;
		this.methodName = methodName;
		this.paramsMap = paramsMap;
	}

	@Override
	public void write() {
		super.write();
		DataUtil.writeUtf8(buffer, interfaceName);
		DataUtil.writeUtf8(buffer, methodName);
		DataUtil.writeMap(buffer, paramsMap);
	}

	@Override
	public void read(ByteBuf buf) {
		this.buffer = buf;
		super.read(buf);
		interfaceName = DataUtil.readUtf8(buf);
		methodName = DataUtil.readUtf8(buf);
		paramsMap = readMap();
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<Pair<String, Object>> getParamsMap() {
		return paramsMap;
	}

	// if the message is request or response   if (is request  and if is response )
}
