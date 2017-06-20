using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

class ConfManager : Singleton<ConfManager>
{
    private static HeroInfoProvider hero = new HeroInfoProvider();



    public static bool loadAllConfig()
    {
        hero.loadConfig();
        return true;
    }

    public static HeroInfoProvider getHeroConf()
    {
        return hero;
    }

}
