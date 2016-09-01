using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.Net.Sockets;

namespace WzNet
{
    public class NetManager: Singleton<NetManager>
    {
        private string remoteAddr;

        private short port;

        private Socket socket;

        private NetStatus status;

        private Queue<RequestMessage> requestMessageQueue = new Queue<RequestMessage>();

        private ByteBuffer recvBuffer;

        private IPEndPoint endPoint;

        byte[] recvBytes = new byte[512];

        public void connect(string host , short port)
        {
            this.remoteAddr = host;
            this.port = port;
            endPoint = new IPEndPoint(IPAddress.Parse(remoteAddr), port);
            socket = new Socket(endPoint.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            socket.Blocking = false;
            socket.NoDelay = true;
            socket.Connect(host, port);
        }


        public void onUpdate()
        {
            procesOutput();
            processInput();
        }

        public void processInput()
        {
            if(socket.Available <= 0 )
            {
                return;
            }
            int count = socket.Receive(recvBytes , recvBytes.Length , SocketFlags.None);
            if(count > 0)
            {
                UnityEngine.Debug.Log("HAHAHAHA");
            }
        }

        void onReceive(object sender ,SocketAsyncEventArgs e)
        {
            if( e.BytesTransferred > 0)
            {
                byte[] bytes = e.Buffer;
                recvBuffer.writeBytes(bytes, 0, e.BytesTransferred);
            }
        }

        public void procesOutput()
        {
            if(requestMessageQueue.Count > 0)
            {
                socket.Send(requestMessageQueue.Dequeue().getBytes());
            }
        }


        public void pushMessage(RequestMessage message)
        {
            requestMessageQueue.Enqueue(message);
        }

        public void pushMessageSync(RequestMessage mesage)
        {
            socket.Send(mesage.getBytes());
        }
    }

    enum NetStatus
    {
        CLOSE,
        CONNECTING,
        CONNECTED,
    }

}
