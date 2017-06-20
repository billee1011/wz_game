using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

interface IdConf
{
    int getId();
}

interface ParseConf
{
    bool parse();
}

interface IdParseConf : IdConf, ParseConf
{
}
