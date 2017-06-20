using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Pair<E, F>
{
    private E k;

    private F v;

    public E Key
    {
        get { return k; }
        set { this.k = value; }
    }

    public F Value
    {
        get { return v; }
        set { this.v = value; }
    }

    public Pair(E key, F value)
    {
        this.k = key;
        this.v = value;
    }

    public string toString()
    {
        return "key : " + k + " value : " + v;
    }

}

