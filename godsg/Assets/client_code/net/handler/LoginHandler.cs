using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using P3Net;
using Proto;
using Google.Protobuf;

class LoginHandler : HandlerBase
{
    public override void RegisterPacket()
    {
        PacketFacotry.Instance.AddPacket((UInt16)PACKET_TYPE.PACKET_GC_LOGIN_SUCC , typeof(Packet), OnReceiveLogin);
        PacketFacotry.Instance.AddPacket((UInt16)PACKET_TYPE.PACKET_GC_TEST_RECEIVE, typeof(Packet), OnReceiveTestMsg);
    }

    public void OnReceiveLogin(Packet packet)
    {
        byte[] bytes = packet.GetBytes();


        PBLoginSucc pb = PBLoginSucc.Parser.ParseFrom(bytes);

        List<KeyValuePair<int,long>> resMap = pb.ResMap.ToList();

        for (int i = 0, count = resMap.Count; i < count; i ++)
        {
            UnityEngine.Debug.Log("the key is  +" + resMap[i].Key + " the value is +" + resMap[i].Value);
        }

        UnityEngine.Debug.LogError("fuck  the result is " + pb.PlayerId);

        PBStringList testPB = new PBStringList();
        testPB.List.Add("a");
        testPB.List.Add("b");
        testPB.List.Add("c");
        Packet testPacket = new Packet(PACKET_TYPE.PACKET_CL_TEST,MessageExtensions.ToByteArray(testPB ));
        NetClient.GetInstance().PushPacket(testPacket);
        
    }

    public void OnReceiveTestMsg(Packet packet)
    {
        byte[] bytes = packet.GetBytes();
        PBStringList request = PBStringList.Parser.ParseFrom(bytes);

        List<string> list = request.List.ToList<string>();
        for(int i = 0, count = list.Count; i < count; i++)
        {
            UnityEngine.Debug.Log("the string is " + list[i]);
        }
    }
}
