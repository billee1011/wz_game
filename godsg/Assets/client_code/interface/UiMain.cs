using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine.UI;

class UiMain : UIPanelBase
{
    Text labelSilver = null;
    Text labelYb = null;
    Text labelTili = null;
    Text labelJingli = null;
    Text labelBattleScore = null;
    Button headButton = null;

    void Awake()
    {
        labelSilver = ObjectCommon.GetChildComponent<Text>(gameObject, "top/label_sivler");
        labelYb = ObjectCommon.GetChildComponent<Text>(gameObject, "top/label_yuanbao");
        labelTili = ObjectCommon.GetChildComponent<Text>(gameObject, "top/label_tili");
        labelJingli = ObjectCommon.GetChildComponent<Text>(gameObject, "top/label_jingli");
        labelBattleScore = ObjectCommon.GetChildComponent<Text>(gameObject, "top/label_zhandouli_value");
    }

    public override void OnShow(params object[] paramsList)
    {
        Debuger.Log("open ui main window");
        labelSilver.text = PlayerManager.Instance.GetResCount(EMoney.SILVER).ToString();
        labelYb.text = PlayerManager.Instance.GetResCount(EMoney.DIAMOND).ToString();
        labelTili.text = PlayerManager.Instance.getTili().ToString();
        labelJingli.text = PlayerManager.Instance.getJingli().ToString();
        labelBattleScore.text = PlayerManager.Instance.getBattleScore().ToString();
    }
    public override void OnClose() { }
    public override void OnRemove() { }
}

