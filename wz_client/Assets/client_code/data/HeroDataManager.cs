using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Wz.Data
{
    public class HeroDataManager:Singleton<HeroDataManager>
    {
        private Dictionary<int, Hero> heroMap = new Dictionary<int, Hero>();
        public void loadConfig(string str)
        {
            
        }
    }
}
