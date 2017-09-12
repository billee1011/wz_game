

namespace P3Net
{
    
	public enum PACKET_TYPE
	{
		PACKET_TYPE_NONE = 0,	//-- 消息空，默认值

		//client 2 server
        PACKET_PING = 100,
		PACKET_CLLogin = 101,
        PACKET_CL_TEST = 103,

		
	    //server 2 client
        PACKET_GC_LOGIN_SUCC = 30101,
        PACKET_GC_TEST_RECEIVE = 30102,
	}
}
