using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Proto;


class PlayerManager : Singleton<PlayerManager>
{
    private Dictionary<EMoney, long> resMap = new Dictionary<EMoney, long>();

    private int userId;

    private long playerId;

    private string name;

    private long battleScore; 

    private int tili;

    private int jingli;

    private Formation[] formations;

    private long[] partners;

    private long[] battleFormation;

    public void InitFromLoginData(PBLoginSucc pb)
    {
        this.playerId = pb.PlayerId;
        this.userId = pb.UserId;
        this.name = pb.Name;
        this.tili = pb.Tili;
        this.jingli = pb.Jingli;
        this.battleScore = pb.BattleScore;
        resMap.Clear();
        List<KeyValuePair<int,long>> list = pb.ResMap.ToList<KeyValuePair<int, long>>();
        for(int i = 0, count = list.Count; i< count; i++)
        {
            resMap.Add((EMoney)list[i].Key, list[i].Value);
        }
        for(int i = 0 , count = pb.HeroList.Count; i < count; i++)
        {
            HeroManager.Instance.addHeroEntity(pb.HeroList[i]);
        }
        for (int i = 0, count = pb.EquipList.Count; i < count; i++)
        {
            EquipManager.Instance.addEquipEntity(pb.EquipList[i]);
        }
        formations = new Formation[6];
        for (int i = 0, count = pb.Formation.Formation.Count; i < count; i++)
        {
            PBOneFormation formationPb = pb.Formation.Formation[i];
            formations[i] = new Formation();
            formations[i].HeroId = formationPb.HeroId;
            formations[i].PetId = formationPb.PetId;
            formations[i].HorseId = formationPb.HorseId;
            formations[i].MingjiangId = formationPb.MingjiangId;
            for(int j = 0 ,equipCount = formationPb.Equip.Count;j < count; j++)
            {
                formations[i].EquipList[i] = formationPb.Equip[i];
            }
        }
        partners = pb.Formation.Partner.ToArray<long>();
        battleFormation = pb.Formation.BattleFormation.ToArray<long>();
    }

    public long getHeroIdByFormationIndex(int index)
    {
        if (index < 0 || index >= formations.Length)
            return 0L;
        if(formations[index] == null)
            return 0L;
        return formations[index].HeroId;
    }

    public long GetResCount(EMoney type)
    {
        if(resMap.ContainsKey(type))
        {
            return resMap[type];
        }
        return 0;
    }

    public int getTili()
    {
        return this.tili;
    }

    public int getJingli()
    {
        return this.jingli;
    }

    public long getBattleScore()
    {
        return this.battleScore;
    }

    public string getName()
    {
        return this.name;
    }
}

