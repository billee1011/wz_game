using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


class MiscUtil
{

    public static List<Pair<int, int>> getPairList(string str, char synmbol1, char symbol2)
    {
        List<Pair<int, int>> result = null;
        if (str == null || str.Equals(""))
        {
            return null;
        }
        string[] strs = str.Split(synmbol1);
        for (int i = 0, length = strs.Length; i < length; i++)
        {
            string[] subStrs = strs[i].Split(symbol2);
            if (subStrs.Length != 2)
            {
                continue;
            }
            int key = int.Parse(subStrs[0]);
            int value = int.Parse(subStrs[1]);
            Pair<int, int> pair = new Pair<int, int>(key, value);
            if (result == null)
            {
                result = new List<Pair<int, int>>();
            }
            result.Add(pair);
        }
        return result;
    }
}
