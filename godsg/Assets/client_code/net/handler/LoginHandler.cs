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
        PlayerManager.GetInstance().InitFromLoginData(pb);
        UnityEngine.Debug.LogError("fuck  the result is " + pb.PlayerId);
        GameUiManager.getInst().showWindow(PanelType.UILogin, false);
        // login success and begin to do something ;
        GameUiManager.getInst().showWindow(PanelType.UIMain_bottom, true);
        GameUiManager.getInst().showWindow(PanelType.UIMain, true);

        VoiceManager.Instance.PlayMusic("world01");

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
