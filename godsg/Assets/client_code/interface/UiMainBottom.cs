using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine.UI;

class UiMainBottom : UIPanelBase
{
    Button buttonMain;
    Button buttonZhenrong;
    Button buttonFuben;
    Button buttonZhengzhan;
    Button buttonBag;
    Button buttonShop;

    void Awake()
    {
        buttonMain = ObjectCommon.GetChildComponent<Button>(gameObject, "button_group/main_view");
        buttonZhenrong = ObjectCommon.GetChildComponent<Button>(gameObject, "button_group/zhenrong");
        buttonFuben = ObjectCommon.GetChildComponent<Button>(gameObject, "button_group/fuben");
        buttonZhengzhan = ObjectCommon.GetChildComponent<Button>(gameObject, "button_group/zhengzhan");
        buttonBag = ObjectCommon.GetChildComponent<Button>(gameObject, "button_group/bag");
        buttonShop = ObjectCommon.GetChildComponent<Button>(gameObject, "button_group/shop");


        buttonMain.onClick.AddListener(OnMainButtonClicked);

        buttonZhenrong.onClick.AddListener(OnZhenrongButtonClicked);
    }

    void OnZhenrongButtonClicked()
    {
        GameUiManager.getInst().showWindow(PanelType.UIMain, false);
        GameUiManager.getInst().showWindow(PanelType.UIFormation, true);
    }

    void OnMainButtonClicked()
    {
        Debuger.Log("main button clicked ");

        GameUiManager.getInst().showWindow(PanelType.UIMain, false);
        GameUiManager.getInst().showWindow(PanelType.UIHeroList, true);
    }

    public override void OnShow(params object[] paramsList)
    {
    }
    public override void OnClose() { }
    public override void OnRemove() { }
}

