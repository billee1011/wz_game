

using System;
using System.Collections.Generic;
using P3GameClient;

namespace P3Net
{
    using PacketID_t = System.UInt16;

    public delegate void OnPacketHandler(Packet packet);

    class PacketFacotry : Singleton<PacketFacotry>
    {
        private class PacketTypeAndHandler
        {
            public Type m_packetType;
            public OnPacketHandler m_handler;
            public PacketTypeAndHandler(Type packetType, OnPacketHandler handler)
            {
                m_packetType = packetType;
                m_handler = handler;
            }
        }

        private Dictionary<UInt16, PacketTypeAndHandler> m_Dictionary = new Dictionary<PacketID_t, PacketTypeAndHandler>();

        //-- 创建消息
        public Packet CreatePacket(PacketID_t packetID)
        {
            //-- 01. valid
            if (!m_Dictionary.ContainsKey(packetID))
            {
                //-- log
                UnityEngine.Debug.Log(" don't contain  the id " + packetID);
                return null;
            }

            PacketTypeAndHandler tmpProcessInfo = m_Dictionary[packetID];
            if (tmpProcessInfo.m_packetType == null)
            {
                //-- log
                return null;
            }
            return new Packet((PACKET_TYPE)packetID);

        }

        //-- 处理消息，调用消息的Handler
        public void ProcessPacket(Packet packet)
        {
            //-- 01. valid
            if (packet == null)
            {
                UnityEngine.Debug.LogError(" null packet when process");
                return;
            }
            if (!m_Dictionary.ContainsKey((UInt16)packet.GetPacketID()))
            {
                UnityEngine.Debug.LogError("dont't contain the packet id {}" +  packet.GetPacketID());
                return;
            }
            PacketTypeAndHandler tmpProcessInfo = m_Dictionary[(UInt16)packet.GetPacketID()];
            if (tmpProcessInfo.m_handler == null)
            {
                //-- log
                return;
            }

            tmpProcessInfo.m_handler.Invoke(packet);
            return;

        }

        public void AddPacket(PacketID_t packetID, Type type, OnPacketHandler packetHandler)
        {
            if (m_Dictionary.ContainsKey(packetID))
            {
                //-TODO: log warning
                m_Dictionary[packetID] = new PacketTypeAndHandler(type, packetHandler);
                return;
            }
            else
            {
                m_Dictionary.Add(packetID, new PacketTypeAndHandler(type, packetHandler));
            }
        }

        public void removePacket(PacketID_t packetID)
        {
            m_Dictionary.Remove(packetID);
        }

        public void removeAllPacket()
        {
            m_Dictionary.Clear();
        }



    }

}
