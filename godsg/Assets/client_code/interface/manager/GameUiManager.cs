using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;

class GameUiManager : MonoBehaviour
{
    static GameUiManager instance;

    public static GameUiManager getInst()
    {
        if (instance == null)
        {
            GameObject obj = GameObject.Find("GUI");
            if( obj == null)
            {
                return instance;
            }
            GameObject.DontDestroyOnLoad(obj);
            Transform tempTrans = obj.transform;
            tempTrans.localScale = Vector3.zero;
            instance = obj.AddComponent<GameUiManager>();
            instance.Init();
        }
        return null;
    }

    void Init()
    {

    }
}
