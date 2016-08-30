using UnityEngine;
using System.Collections;
using System.Net.Sockets;
using System.Net;
using System.Text;
using WzBuffer;

public class Main : MonoBehaviour {
    private Socket so = null;


    void Awake()
    {
        Object obj = Resources.Load("hello", typeof(TextAsset)) as TextAsset;

        ByteBuffer buffer = new ByteBuffer(128);

        buffer.writeByte(-11);
        buffer.writeChar(70);
        buffer.writeInt(-1234);
        buffer.writeLong(13243535L);
        buffer.writeFloat(232535.32f);


        int a = buffer.readByte();
        char b = buffer.readChar();
        int c = buffer.readInt();
        long d = buffer.readLong();
        float e = buffer.readFloat();

        UnityEngine.Debug.Log(a);
        UnityEngine.Debug.Log(b);
        UnityEngine.Debug.Log(c);
        UnityEngine.Debug.Log(d);
        UnityEngine.Debug.Log(e);
    }

	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
	    
	}
}
