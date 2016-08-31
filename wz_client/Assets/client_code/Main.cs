using UnityEngine;
using System.Collections;
using System.Net.Sockets;
using System.Net;
using System.Text;
using WzNet;

public class Main : MonoBehaviour {
    private Socket so = null;
    private int count = 0;

    void Awake()
    {
        Object obj = Resources.Load("hello", typeof(TextAsset)) as TextAsset;
        NetManager.getInst().connect("127.0.0.1", (short)15010);

        NetManager.getInst().pushMessageSync(new RequestMessage(1, 2));


    }

	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
        NetManager.getInst().onUpdate();

    }
}
