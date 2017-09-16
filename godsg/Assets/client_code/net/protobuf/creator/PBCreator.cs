using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Proto;

class PBCreator
{
    public static PBInt64 Int64(long value)
    {
        PBInt64 pb = new PBInt64();
        pb.Value = value;
        return pb;
    }  
}

