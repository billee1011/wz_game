using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class UiTab : MonoBehaviour {

    public Toggle[] toggleList;

    public GameObject[] objectList;
    
    // Use this for initialization

    private void Awake()
    {
        for(int i = 0, length = toggleList.Length; i < length; i++)
        {
            toggleList[i].onValueChanged.AddListener(OnToggleValueChanged);
        }
    }


    void OnToggleValueChanged(bool isOn)
    {
        if(!isOn)
        {
            return;
        }
        int index = 0;
        for (int i = 0, length = toggleList.Length; i < length; i++)
        {
            if (toggleList[i].isOn)
            {
                index = i;
            }
        }
        for (int i = 0, length = objectList.Length; i < length; i++)
        {
            if (i != index)
                objectList[i].SetActive(false);
            else
                objectList[i].SetActive(true);
        }
    }

    // Update is called once per frame
    void Update () {
		
	}
}
