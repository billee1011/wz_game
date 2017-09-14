using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;


class TestGridLayOut : MonoBehaviour 
{

    void Awake()
    {
        addItem(1);
        addItem(2);
        addItem(3);
        addItem(4);
        addItem(5);
        addItem(6);
    }


    public void addItem(int index)
    {
        GameObject prefabObject = ObjectCommon.GetChild(gameObject, "item");

        GameObject childObject = GameObject.Instantiate(prefabObject);
        childObject.name = "item" + index;
        childObject.transform.SetParent(gameObject.transform);
        childObject.transform.localPosition = Vector2.zero;
        childObject.transform.localScale = Vector2.one;
        childObject.SetActive(true);
    }
}
