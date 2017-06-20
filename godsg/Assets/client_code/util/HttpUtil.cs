using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;

class HttpUtil : Singleton<HttpUtil>
{
    public delegate void ResponseHandler(string result);

    private WWW httpRequest = null;

    private ResponseHandler callBack = null;

    private bool downloading;

    public void sendGetRequest(string url, ResponseHandler handler, int timeout, WWWForm form = null)
    {
        if(null != form)
        {
            httpRequest = new WWW(url, form);
        }
        else
        {
            httpRequest = new WWW(url);
        }
        callBack = handler;
        downloading = true;
    }

    public void OnUpdate()
    {
        if (!downloading)
        {
            return;
        }
        if(httpRequest.isDone)
        {
            UnityEngine.Debug.Log("request is done ");
            if(callBack != null)
            {
                callBack(httpRequest.text);
            }
            downloading = false;
        }
    }
}
