using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.EventSystems;
using P3GameClient;

class UiFormation : UIPanelBase
{

    Text labelLevel;
    Text labelAttack;
    Text labelHp;
    Text labelPhysicDefence;
    Text labelMagicDefence;
    EventTrigger eventTrigger;

    Button btn;
    private void Awake()
    {
        GameObject bottomLeftObj = ObjectCommon.GetChild(gameObject, "bottom/left");
        labelLevel = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "level_value");
        labelAttack = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "attack_value");
        labelHp = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "hp_value");
        labelPhysicDefence = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "phy_defence_value");
        labelMagicDefence = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "mag_defence (1)");
        GameObject topObj = ObjectCommon.GetChild(gameObject, "equip_list");

        eventTrigger = ObjectCommon.GetChildComponent<EventTrigger>(topObj, "equip_head");

        btn = ObjectCommon.GetChildComponent<Button>(topObj, "equip_head");
        btn.onClick.AddListener(OnEquipHeadClicked);

        ClientEventManager.GetInstance().AddProcessFunction(GameEventID.GEDynamicHudPos_Add, OnEquipWeared);
        
    }

    void OnEquipWeared(ClientEvent e)
    {
     
    }

    void OnEquipHeadClicked()
    {
        Debuger.Log(" btn clicked and equipment ");
    }

    public void OnEquipClick()
    {

    }

    public override void OnShow(params object[] paramsList)
    {
        HeroEntity entity = HeroManager.GetInstance().getHeroEntity(0);
        if( entity == null)
        {
            Debuger.LogError(" the hero entity is null");
        }
        labelLevel.text = entity.Level.ToString();
        labelAttack.text = entity.GetAttributeValue(EBattleAttribute.ATTACK).ToString();
    }

    public override void OnClose()
    {

    }

    public override void OnRemove()
    {
        ClientEventManager.GetInstance().RemoveProcessFunction(GameEventID.GEDynamicHudPos_Add, OnEquipWeared);
    }
}

