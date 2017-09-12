using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;

class GameUiManager : MonoBehaviour
{

    public class WindowActiveTimeData
    {
        public string panelName;
        public bool isActive;
        public long inActiveTime;
        public long activeTime;
        public UIPanelBase panel;

        public WindowActiveTimeData(string panelName, bool isActive, long inActiveTime, long activeTime, UIPanelBase panel)
        {
            this.panelName = panelName;
            this.isActive = isActive;
            this.inActiveTime = inActiveTime;
            this.activeTime = activeTime;
            this.panel = panel;
        }
    }

    private int maxWindowNum = 15;

    private List<string> g_LoadingWindow = new List<string>();

    private Dictionary<string, WindowActiveTimeData> g_LoadedWindow = new Dictionary<string, WindowActiveTimeData>();

    static GameUiManager instance;

    public static Transform UIRootTran
    {
        get { return instance.gameObject.transform; }
    }

    public static GameUiManager getInst()
    {
        if (instance == null)
        {
            GameObject obj = GameObject.Find("Canvas");
            if( obj == null)
            {
                return instance;
            }
            GameObject.DontDestroyOnLoad(obj);
            Transform tempTrans = obj.transform;
            tempTrans.localScale = Vector3.zero;
            instance = obj.AddComponent<GameUiManager>();
            instance.Init();
            
        }
        return instance;
    }

    void Init()
    { 

    }


    private UIPanelBase GetExistWindow(string name)
    {
        if (g_LoadedWindow == null || !g_LoadedWindow.ContainsKey(name))
            return null;

        return g_LoadedWindow[name].panel;
    }


    public  void showWindow(string name, bool active, params object[] paramsList)
    {
        UIPanelBase panel = GetExistWindow(name);
        if (panel != null && panel.gameObject != null)
        {
            openWindow(panel, active, paramsList);
        }
        else
        {
            if (active)
            {
                downloadWindow(name, paramsList);
            }
        }
    }

    private void downloadWindow(string name, params object[] paramsList)
    {
        if (g_LoadedWindow == null)
        {
            return;
        }
        //addActionWindow(name);
        if (g_LoadingWindow.Contains(name))
        {
            return;
        }
        addLoadingWindow(name);
        ResourceManager.getInstance().LoadAsync(name, "gui", paramsList);
    }

    public void addLoadingWindow(string name)
    {
        if (g_LoadingWindow == null)
        {
            return;
        }
        if (g_LoadingWindow.Contains(name))
        {
            return;
        }

        g_LoadingWindow.Add(name);
    }

    public void removeLoadingWindow(string name)
    {
        if (g_LoadingWindow == null)
        {
            return;
        }
        if (g_LoadingWindow.Contains(name) == false)
        {
            return;
        }

        g_LoadingWindow.Remove(name);
    }

    public void addLoadedWindow(string name, UIPanelBase panel, bool isActive)
    {
        if (g_LoadedWindow == null)
        {
            return;
        }

        long inactiveTime = 0;
        if (isActive)
            inactiveTime = 0;
        else
            inactiveTime = DateTime.Now.Ticks;

        if (g_LoadedWindow.ContainsKey(name))
        {
            g_LoadedWindow[name].isActive = isActive;
            g_LoadedWindow[name].inActiveTime = inactiveTime;
            g_LoadedWindow[name].activeTime = DateTime.Now.Ticks;
            g_LoadedWindow[name].panel = panel;
            return;
        }

        g_LoadedWindow.Add(name, new WindowActiveTimeData(name, isActive, inactiveTime, DateTime.Now.Ticks, panel));
    }

    private void openWindow(UIPanelBase panel, bool active, params object[] paramsList)
    {
        Debuger.Log("begin open window and the pannel is " + panel.name);
        if (panel == null || panel.gameObject == null)
        {
            Debuger.LogError("the panel is null");
            return;
        }

        if (g_LoadedWindow == null)
        {
            Debuger.LogError("the loaded window is null");
            return;
        }

        // change window status
        if (active)
        {
            //DontUseThisShowPanel(panel, paramsList);
        }
        else
        {
            Debuger.Log("show window false and the panel name is " + panel.name);
            if (panel.gameObject.activeInHierarchy)
            {
                panel.OnClose();
                panel.gameObject.SetActive(false);
                ManageActiveWindow(false, panel);
            }

  
        }
    }

    public void ManageActiveWindow(bool isActive, UIPanelBase panel)
    {
        ManageActiveWindow(isActive, panel, maxWindowNum);
    }

    public void ManageActiveWindow(bool isActive, UIPanelBase panel, int maxNum)
    {
        if (g_LoadedWindow == null)
        {
            return;
        }

        string panelName = panel.gameObject.name;

        if (g_LoadedWindow.ContainsKey(panelName))
        {
            g_LoadedWindow[panelName].isActive = isActive;
            g_LoadedWindow[panelName].panelName = panelName;
            g_LoadedWindow[panelName].activeTime = DateTime.Now.Ticks;
            if (isActive)
                g_LoadedWindow[panelName].inActiveTime = 0;
            else
            {
                g_LoadedWindow[panelName].inActiveTime = DateTime.Now.Ticks;
            }
        }
        else
        {
            long inactiveTime = 0;
            if (isActive)
                inactiveTime = 0;
            else
            {
                inactiveTime = DateTime.Now.Ticks;
            }

            g_LoadedWindow.Add(panelName, new WindowActiveTimeData(panelName, isActive, DateTime.Now.Ticks, inactiveTime, panel));
        }

        RemoveBeyondInActiveWindows(maxNum);
    }

    public void RemoveBeyondInActiveWindows(int maxNum)
    {
     
    }




}
