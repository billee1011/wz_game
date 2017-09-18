using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

class Formation
{
    private long heroId;

    private long[] equipList;

    private long petId;

    private long horseId;

    private long mingjiangId;

    public Formation()
    {
        this.heroId = 0;
        this.equipList = new long[6];
        this.petId = 0;
        this.horseId = 0;
        this.mingjiangId = 0;
    }

    public long HeroId
    {
        get
        {
            return heroId;
        }

        set
        {
            heroId = value;
        }
    }

    public long[] EquipList
    {
        get
        {
            return equipList;
        }

        set
        {
            equipList = value;
        }
    }

    public long PetId
    {
        get
        {
            return petId;
        }

        set
        {
            petId = value;
        }
    }

    public long HorseId
    {
        get
        {
            return horseId;
        }

        set
        {
            horseId = value;
        }
    }

    public long MingjiangId
    {
        get
        {
            return mingjiangId;
        }

        set
        {
            mingjiangId = value;
        }
    }
}
