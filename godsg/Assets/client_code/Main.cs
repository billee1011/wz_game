using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.IO;
using Google.Protobuf;
using LitJson;

public class Main : MonoBehaviour
{

    private float intervalTime = 0.0f;

    void Awake()
    {
      
        //要开始的逻辑是什么呢
    }

    // Use this for initialization
    void Start()
    {

        TextAsset asset = Resources.Load("HeroBase") as TextAsset;
        if (asset == null)
        {
            
        }
        HeroInfoProvider.Instance.LoadConfig(asset.text);


        //HttpUtil.Instance.sendGetRequest("http://localhost:10024/fast_login?machine_id=111111&channel=123&device=android", onLoginComplete, 3000);

        GameUiManager.getInst();
        PanelManager.GetInstance().registerAllPanel();


        GameUiManager.getInst().showWindow(PanelType.UILogin, true);
        
    }

    void onLoginComplete(string haha)
    {
        UnityEngine.Debug.LogError(" the result is " + haha);
        
    }

    // Update is called once per frame
    void Update()
    {
        HttpUtil.Instance.OnUpdate();
        P3Net.NetClient.Instance.Update(Time.deltaTime);

        intervalTime += Time.deltaTime;

        if( intervalTime > 5)
        {
            P3Net.NetClient.GetInstance().PushPacket(new P3Net.Packet(P3Net.PACKET_TYPE.PACKET_PING, (byte[])null));
            intervalTime = 0;
        }
    }

  
}
