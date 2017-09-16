using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

class EquipEntity
{
    private long equipId;

    private int confId;

    private int level;

    private int jinglianLevel;

    private int jinglianExp;

    private int starLevel;

    private int starExp;

    private int starBless;

    private int goldLevel;

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

    public int Level
    {
        get
        {
            return level;
        }

        set
        {
            level = value;
        }
    }

    public int JinglianLevel
    {
        get
        {
            return jinglianLevel;
        }

        set
        {
            jinglianLevel = value;
        }
    }

    public int JinglianExp
    {
        get
        {
            return jinglianExp;
        }

        set
        {
            jinglianExp = value;
        }
    }

    public int StarLevel
    {
        get
        {
            return starLevel;
        }

        set
        {
            starLevel = value;
        }
    }

    public int StarExp
    {
        get
        {
            return starExp;
        }

        set
        {
            starExp = value;
        }
    }

    public int StarBless
    {
        get
        {
            return starBless;
        }

        set
        {
            starBless = value;
        }
    }

    public int GoldLevel
    {
        get
        {
            return goldLevel;
        }

        set
        {
            goldLevel = value;
        }
    }

    public long EquipId
    {
        get
        {
            return equipId;
        }

        set
        {
            equipId = value;
        }
    }

    public int ConfId
    {
        get
        {
            return confId;
        }

        set
        {
            confId = value;
        }
    }
}
