using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


class HeroEntity
{
    private long entityId;

    private int confId;

    private int tianmingLevel;

    private int awakeLevel;

    private int[] awakeInfo;

    private int huashenLevel;

    private int level;

    private int exp;

    public int GetAttributeValue(EBattleAttribute type)
    {
        if (!attributes.ContainsKey(type))
        {
            return 0;
        }
        else
        {
            return attributes[type];
        }
    }

    private Dictionary<EBattleAttribute, int> attributes;

    public Dictionary<EBattleAttribute, int> Attributes
    {
        get
        {
            return this.attributes;
        }
        set
        {
            this.attributes = value;
        }
    }

    public long EntityId
    {
        set
        {
            this.entityId = value;
        }
        get
        {
            return this.entityId;
        }
    }


    public int ConfId
    {
        set
        {
            this.confId = value;
        }
        get
        {
            return this.confId;
        }
    }

    public int TianmingLevel
    {
        set
        {
            this.tianmingLevel = value;
        }
        get
        {
            return this.tianmingLevel;
        }
    }

    public int Level
    {
        set
        {
            this.level= value;
        }
        get
        {
            return this.level;
        }
    }

    public int Exp
    {
        set
        {
            this.exp = value;
        }
        get
        {
            return this.exp;
        }
    }

    public int AwakeLevel
    {
        set
        {
            this.awakeLevel = value;
        }
        get
        {
            return this.awakeLevel;
        }
    }

    public int HuashenLevel
    {
        get
        {
            return this.huashenLevel;
        }
        set
        {
            this.huashenLevel = value;
        }
    }

}

