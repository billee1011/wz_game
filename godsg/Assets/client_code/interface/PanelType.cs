using UnityEngine;
using System.Collections;

public class PanelType {
	// the panel's prefab name
	public static string UILogin = "UiLogin";
    public static string UIMain_bottom = "Ui_Main_Bottom";
    public static string UIMain = "Ui_Main";
    public static string UIHeroList = "Ui_Hero_List";
    public static string UIFormation = "Ui_Formation";
}

public class PanelDepth
{
	public static int PD_MainScene = 0;
	public static int PD_MainHead = 10000;
	public static int PD_PDLayer_1 = 20000;
	public static int PD_PDLayer_1_5 = 25000;
	public static int PD_PDLayer_2 = 30000;
	public static int PD_PDLayer_2_5 = 35000;
	public static int PD_PDLayer_3 = 40000;
	public static int PD_PDLayer_4 = 50000;
	public static int PD_PDLayer_5 = 60000;
	public static int PD_PDLayer_6 = 61000;
	public static int PD_ToolBar = 5000;
	public static int PD_Dialog = 70000;
	public static int PD_Chat = 72500;
	public static int PD_Announce = 75000;
	public static int PD_Mask = 80000;
	public static int PD_Connect = 80900;
	public static int PD_Loading = 81000;
	public static int PD_AdapterMask = 90000;
}
