using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Proto;
using Google.Protobuf;
using Google.Protobuf.Collections;

class HeroManager : Singleton<HeroManager>
{

    private Dictionary<long, HeroEntity> heroMap = new Dictionary<long, HeroEntity>();



    public void addHeroEntity(PBHeroEntity pb)
    {
        HeroEntity entity = new HeroEntity();
        entity.EntityId = pb.HeroId;
        entity.ConfId = pb.ConfId;
        entity.Level = pb.Level;
        entity.Exp = pb.Exp;
        entity.AwakeLevel = pb.AwakeLevel;
        entity.TianmingLevel = pb.TianmingLevel;
        Dictionary<EBattleAttribute, int> attrs = new Dictionary<EBattleAttribute, int>();
        foreach(KeyValuePair<int,int> keyValue in pb.Attributes.ToList<KeyValuePair<int,int>>()){
            attrs.Add((EBattleAttribute)keyValue.Key, keyValue.Value);
        }
        entity.Attributes = attrs;
        heroMap.Add(entity.EntityId, entity);
        
    }

    public HeroEntity getHeroEntity(int formationIndex)
    {
        long heroId = PlayerManager.GetInstance().getHeroIdByFormationIndex(formationIndex);
        Debuger.Log("the hero id is " + heroId);
        if (!heroMap.ContainsKey(heroId))
        {
            return null;
        }
        return heroMap[heroId];
    }

}
