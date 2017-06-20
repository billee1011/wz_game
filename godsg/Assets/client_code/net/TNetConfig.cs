

namespace P3Net
{
    using System;
    using PacketID_t = System.UInt16;

    class TNetConfig
    {
        //-- 测试数据，需要的时候在进行调整。
        public static int DEFAULT_SOCKET_OUTPUT_BUFFERSIZE = 8 * 1024;
        public static int DEFAULT_SOCKET_INPUT_BUFFERSIZE = 64 * 1024;

        public static int DISCONNECT_SOCKET_OUTPUT_BUFFERSIZE = 100 * 1024;

        public static int DEFAULT_SOCKET_CONNECT_RETRY_CNT = 2;
        public static int DEFAULT_SOCKET_CONNECT_OVERTIME_MILLSEC = 10 * 1000;

        public static int DEFAULT_SOCKET_SEND_ASYNC_WAITING_TIME = 100 * 1000;
        public static int DEFAULT_SOCKET_RECEIVE_ASYNC_WAITING_TIME = 10 * 1000;

        public static int DEFAULT_SOCKET_SEND_QUEUE_SIZE = 1;
        public static int DEFAULT_SOCKET_RECV_QUEUE_SIZE = 1;   //-- 这里为1，防止在net层tcp消息是按照顺序到达的。但是在逻辑层的处理上，顺序相反。
        public static int DEFAULT_LOGIC_RECV_QUEUE_SIZE = 10;

        public static int DEFAULT_SOCKET_SEND_BUFFERSIZE = 8 * 1024;
        public static int DEFAULT_SOCKET_RECV_BUFFERSIZE = 64 * 1024;

        public static Int32 MAX_LENGTH_ACCOUNT_NAME = 30;
        public static Int32 MAX_LENGTH_PASSWORD = 30;
        public static Int32 MAX_LENGTH_ENTERWORLD_REASON = 100;
        public static Int32 MAX_LENGTH_GM_COMMAND = 100;

        //-TODO: 以后换个位置，不放在这里
        public static int BYTE_LENGTH = 8;
    
    }
}
