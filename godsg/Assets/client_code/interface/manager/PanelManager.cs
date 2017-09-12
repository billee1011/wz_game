using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System;

public class PanelManager : Singleton<PanelManager>
{
	public class PanelModuleData
	{
		public string panelName;
		public Type panelType;
		public int panelDepth;
		public List<string> linkedPanel;
		public List<string> mutexPanel;
		public bool isMutex;
		
		public PanelModuleData(Type panelType, string panelName, int panelDepth)
		{
			this.panelType = panelType;
			this.panelName = panelName;
			this.panelDepth = panelDepth;
			
			if (this.linkedPanel == null)
				this.linkedPanel = new List<string>();
			
			this.isMutex = false;
		}
		
		public PanelModuleData(Type panelType, string panelName, int panelDepth, List<string> linkedPanel)
		{
			this.panelType = panelType;
			this.panelName = panelName;
			this.panelDepth = panelDepth;
			this.linkedPanel = linkedPanel;
			
			if (this.linkedPanel == null)
				this.linkedPanel = new List<string>();
			//this.linkedPanel.Add(PanelType.UITips);
			//this.linkedPanel.Add(PanelType.UITemplate);
			
			this.isMutex = true;
		}
		
		public PanelModuleData(Type panelType, string panelName, int panelDepth, List<string> linkedPanel, bool isShowAllMain)
		{
			this.panelType = panelType;
			this.panelName = panelName;
			this.panelDepth = panelDepth;
			this.linkedPanel = linkedPanel;
			
			if (this.linkedPanel == null)
				this.linkedPanel = new List<string>();
			
			//this.linkedPanel.Add(PanelType.UIMainScene);
			//this.linkedPanel.Add(PanelType.UIMainTopBar);

			//this.linkedPanel.Add(PanelType.UITips);
			//this.linkedPanel.Add(PanelType.UITemplate);
			
			if (isShowAllMain)
			{
				//this.linkedPanel.Add(PanelType.UIMainHead);
				//this.linkedPanel.Add(PanelType.UIMainActivity);

				//this.linkedPanel.Add(PanelType.UIMainMenu);
				//this.linkedPanel.Add(PanelType.UIChat);
			}
			this.isMutex = true;
		}
		
		public PanelModuleData(Type panelType, string panelName, int panelDepth, List<string> linkedPanel, List<string> mutexPanel, bool isMutex)
		{
			this.panelType = panelType;
			this.panelName = panelName;
			this.panelDepth = panelDepth;
			this.linkedPanel = linkedPanel;
			
			if (this.linkedPanel == null)
				this.linkedPanel = new List<string>();

			if (!isMutex)
			{
				//this.linkedPanel.Add(PanelType.UITemplate);
				//this.linkedPanel.Add(PanelType.UITips);
			}
			
			this.mutexPanel = mutexPanel;
			this.isMutex = isMutex;
		}
	}
	
	private static Dictionary<string, PanelModuleData> g_PanelRegisted = new Dictionary<string, PanelModuleData>();
	private static Dictionary<string, PanelModuleData> g_AllPanel = new Dictionary<string, PanelModuleData>();

	private List<string> g_DonotUnload = new List<string>();

	public List<string> DonotUnload {
		get {
			return g_DonotUnload;
		}
	}

	public void RegisterDonotUnload()
	{
		g_DonotUnload.Clear ();

	}
	
	/// <summary>
	/// Init all panels and components.
	/// </summary>
	public void Init()
	{
		RegisterDonotUnload();
		if (g_AllPanel != null)
		{
			g_AllPanel.Clear();
			g_AllPanel.Add(PanelType.UILogin, new PanelModuleData(typeof(UiLogin), PanelType.UILogin, PanelDepth.PD_PDLayer_1));
            g_AllPanel.Add(PanelType.UIMain_bottom, new PanelModuleData(typeof(UiLogin), PanelType.UIMain_bottom, PanelDepth.PD_PDLayer_1));
            g_AllPanel.Add(PanelType.UIMain, new PanelModuleData(typeof(UiMain), PanelType.UIMain, PanelDepth.PD_PDLayer_1));

        }

	}
	
	/// <summary>
	/// Registers all panel.
	/// </summary>
	public void registerAllPanel()
	{
		Init();
		g_PanelRegisted.Clear ();
		foreach (string key in g_AllPanel.Keys)
		{
			registerPanel(key, g_AllPanel[key]);
		}
	}
	
	/// <summary>
	/// Registers the panel.
	/// </summary>
	/// <param name="name">Name.</param>
	/// <param name="component">Component.</param>
	private void registerPanel(string name, PanelModuleData data)
	{
		if (g_PanelRegisted == null)
		{
			return;
		}
		
		if (g_PanelRegisted.ContainsKey(name))
		{
			Debuger.LogWarning("Cover the panel scripts from " + g_PanelRegisted[name] + " to " + data);
		}
		g_PanelRegisted.Add(name, data);
	}
	/// <summary>
	/// Gets the component.
	/// </summary>
	/// <returns>The component.</returns>
	/// <param name="name">Name.</param>
	public PanelModuleData getComponent(string name)
	{
        Debuger.Log("the name is " + name);
		if (g_PanelRegisted == null)
		{
            Debuger.LogError(" pannel is null");
			return null;
		}
		
		if (g_PanelRegisted.ContainsKey(name))
		{
			return g_PanelRegisted[name];
		}
		
		return null;
	}
	
	public bool IsRegistedPanel(string panelName)
	{
		panelName = panelName.Replace("(Clone)", "");
		return g_PanelRegisted.ContainsKey(panelName);
	}
}
