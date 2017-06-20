using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;
using UnityEngine.UI;
using P3Net;
using Proto;


class UiLogin : MonoBehaviour
{
    private InputField userName;
    private InputField password;
    private Button submitButton;
    void Awake()
    {
        userName = gameObject.transform.FindChild("username").GetComponent<InputField>();
        UnityEngine.Debug.Log(" THE user name file di s " + userName);
        submitButton = gameObject.transform.FindChild("Button").GetComponent<Button>();

        submitButton.onClick.AddListener(OnButtonClicked);
    }

    void OnButtonClicked()
    {
        string name = userName.text;
        UnityEngine.Debug.Log("LOGGER the button clicked event " + name);

        HttpUtil.Instance.sendGetRequest("127.0.0.1:10024/login/fast_login?userName=" + name ,OnLoginResult,3000 );
    }

    class LoginResponse
    {
        public string gateHost;
        public int gatePort;
        public int userId;
        public string token;
    }


    void OnLoginResult(string result)
    {
        UnityEngine.Debug.LogError("the result is" + result);
        LoginResponse loginResponse = JsonUtility.FromJson<LoginResponse>(result);
        string[] strs = result.Split(':');
        UnityEngine.Debug.Log(" the result is " + result);
        string host = loginResponse.gateHost;
        int port = loginResponse.gatePort;
        this.userId = loginResponse.userId;
        this.token = loginResponse.token;
        UnityEngine.Debug.Log("becin connect to " + host + ":" + port);
        NetClient.Instance.ConnectService(host, port, OnConnectSucc, OnConnectFailed);
    }

    private int userId;
    private string token;

    void OnConnectSucc()
    {
        UnityEngine.Debug.Log("connect success ");
        PBLoginReq req = new PBLoginReq();
        req.UserId = userId;
        req.Token = token;
        Packet packet = new Packet(PACKET_TYPE.PACKET_CLLogin, req);
        NetClient.Instance.PushPacket(packet);
    }

    void OnConnectFailed()
    {
        UnityEngine.Debug.Log("connect failed");
    }
}
