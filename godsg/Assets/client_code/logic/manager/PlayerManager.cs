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

