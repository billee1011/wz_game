using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace WzNet
{
    public abstract class BaseMessage
    {
        public abstract byte[] getBytes();
    }
}
