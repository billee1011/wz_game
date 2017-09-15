using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;
using UnityEngine.UI;

class UiFormation : UIPanelBase
{

    Text labelLevel;
    Text labelAttack;
    Text labelHp;
    Text labelPhysicDefence;
    Text labelMagicDefence;

    private void Awake()
    {
        GameObject bottomLeftObj = ObjectCommon.GetChild(gameObject, "bottom/left");
        labelLevel = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "level_value");
        labelAttack = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "attack_value");
        labelHp = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "hp_value");
        labelPhysicDefence = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "phy_defence_value");
        labelMagicDefence = ObjectCommon.GetChildComponent<Text>(bottomLeftObj, "mag_defence (1)");
    }

    public override void OnShow(params object[] paramsList)
    {
        HeroEntity entity = HeroManager.GetInstance().getHeroEntity();
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

    }
}

