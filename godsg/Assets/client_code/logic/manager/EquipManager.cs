using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Proto;

class EquipManager : Singleton<EquipManager>
{
    private Dictionary<long, EquipEntity> equipMap = new Dictionary<long, EquipEntity>();


    public void addEquipEntity(PBEquipEntity pb)
    {
        EquipEntity entity = new EquipEntity();
        entity.EquipId = pb.EntityId;
        entity.ConfId = pb.ConfId;
        entity.Level = pb.Level;
        Dictionary<EBattleAttribute, int> attrs = new Dictionary<EBattleAttribute, int>();
        foreach (KeyValuePair<int, int> keyValue in pb.Attts.ToList<KeyValuePair<int, int>>())
        {
            attrs.Add((EBattleAttribute)keyValue.Key, keyValue.Value);
        }
        entity.Attributes = attrs;
        equipMap.Add(entity.EquipId, entity);
    }


    public EquipEntity getEquipEntity(long id)
    {
        if( id < 100000)
        {
            return equipMap.Values.First<EquipEntity>();
        }
        if(equipMap.ContainsKey(id))
        {
            return equipMap[id];
        }
        else
        {
            return null;
        }
    }
}
