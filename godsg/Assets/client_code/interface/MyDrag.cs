using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;

public class MyDrag : MonoBehaviour , IPointerDownHandler , IPointerUpHandler, IDragHandler, IPointerEnterHandler, IPointerExitHandler{
    public RectTransform canvasRect;

    private RectTransform imgRect;       
    Vector2 offset = new Vector3();    
    Vector3 imgReduceScale = new Vector3(0.8f, 0.8f, 1);
    Vector3 imgNormalScale;   


    public void OnDrag(PointerEventData eventData)
    {
        Vector2 mouseDrag = eventData.position;   //当鼠标拖动时的屏幕坐标
        Vector2 uguiPos = new Vector2();   //用来接收转换后的拖动坐标
        //和上面类似
        bool isRect = RectTransformUtility.ScreenPointToLocalPointInRectangle(canvasRect, mouseDrag, eventData.enterEventCamera, out uguiPos);

        if (isRect)
        {
            //设置图片的ugui坐标与鼠标的ugui坐标保持不变
            imgRect.anchoredPosition = offset + uguiPos;
        }
    }

    public void OnPointerDown(PointerEventData eventData)
    {
        Vector2 mouseDown = eventData.position;    //记录鼠标按下时的屏幕坐标
        Vector2 mouseUguiPos = new Vector2();   //定义一个接收返回的ugui坐标
        //RectTransformUtility.ScreenPointToLocalPointInRectangle()：把屏幕坐标转化成ugui坐标
        //canvas：坐标要转换到哪一个物体上，这里img父类是Canvas，我们就用Canvas
        //eventData.enterEventCamera：这个事件是由哪个摄像机执行的
        //out mouseUguiPos：返回转换后的ugui坐标
        //isRect：方法返回一个bool值，判断鼠标按下的点是否在要转换的物体上
        bool isRect = RectTransformUtility.ScreenPointToLocalPointInRectangle(canvasRect, mouseDown, eventData.enterEventCamera, out mouseUguiPos);
        if (isRect)   //如果在
        {
            //计算图片中心和鼠标点的差值
            offset = imgRect.anchoredPosition - mouseUguiPos;
        };
    }

    public void OnPointerEnter(PointerEventData eventData)
    {
        imgRect.localScale = imgReduceScale;
    }

    public void OnPointerExit(PointerEventData eventData)
    {
        imgRect.localScale = imgNormalScale;
    }

    public void OnPointerUp(PointerEventData eventData)
    {
        offset = Vector2.zero;
    }

    // Use this for initialization
    void Start () {
        imgRect = GetComponent<RectTransform>();
        imgNormalScale = imgRect.localScale;
    }
	
	// Update is called once per frame
	void Update () {
		
	}
}
