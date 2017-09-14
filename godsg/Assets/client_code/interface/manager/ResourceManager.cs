using UnityEngine;
using System.Collections;

public class ResourceManager : MonoBehaviour
{

    private static ResourceManager g_Instance = null;

    private bool isUseAB = false;
	//jilu jiemian xianshi shendu
	public static int g_PDInsNb = 0;

	private object[] g_ParamsList;

    public static ResourceManager getInstance()
    {
        if (g_Instance == null)
        {
            string objName = "ResourceManager";
            GameObject resourceManagerObj = UnityEngine.GameObject.Find(objName);

            if (resourceManagerObj == null)
            {
                resourceManagerObj = new GameObject();
                resourceManagerObj.transform.localPosition = Vector3.zero;
                resourceManagerObj.transform.localRotation = Quaternion.identity;
                resourceManagerObj.transform.localScale = Vector3.one;
                resourceManagerObj.name = objName;
            }

            GameObject.DontDestroyOnLoad(resourceManagerObj);
            g_Instance = resourceManagerObj.AddComponent<ResourceManager>();
        }

        return g_Instance;
    }

    public void LoadAsync(string name, string type, params object[] paramsList)
    {
        if (isUseAB)
        {
        }
        else
        {
            StartCoroutine(LoadPrefabFromRes(name, paramsList));
        }
	}

	IEnumerator LoadPrefabFromRes(string name, params object[] paramsList)
    {
		ResourceRequest quest = Resources.LoadAsync("Prefabs/Gui/" + name);
		yield return quest;
		GameObject instance = Instantiate(quest.asset) as GameObject;
		
		Transform instanceTran = instance.transform;
		instanceTran.parent = GameUiManager.UIRootTran;
		instanceTran.localPosition = Vector3.zero;
		instanceTran.localScale = Vector3.one;
        GameObject mainBottomObj = ObjectCommon.GetChild(GameUiManager.UIRootTran.gameObject, PanelType.UIMain_bottom);
        if(mainBottomObj != null)
        {
            mainBottomObj.transform.SetSiblingIndex(instanceTran.GetSiblingIndex());
        }
		
		PanelManager.PanelModuleData panelData = PanelManager.GetInstance().getComponent(name);
        Debuger.LogError("pannel data is null ?" + panelData);
		
		instance.name = name;
		string component = panelData.panelName;
		
		if (string.IsNullOrEmpty(component) == false)
		{
            instance.AddComponent(panelData.panelType);
		}
		
		UIPanelBase panel = instance.gameObject.GetComponent<UIPanelBase>();
		if (instance != null && instance.gameObject != null)
		{
			instance.gameObject.SetActive(true);
		}



		if (panel != null)
		{
            panel.OnShow();
			//GameUiManager.getInst().DontUseThisShowPanel(panel, paramsList);
		}
		

		
		GameUiManager.getInst().addLoadedWindow(name, panel, true);
		GameUiManager.getInst().removeLoadingWindow(name);
    }


  

	public void OnAssetbundleLoadComplete(GameObject instance, string prefab)
	{
		if(instance == null)
		{
			Debug.Log("instance is null!");
			return;
		}
		if(GameUiManager.UIRootTran == null)
		{
			Debug.Log("UIRootTran is null!");
			return;
		}
		Transform instanceTran = instance.transform;
		instanceTran.parent = GameUiManager.UIRootTran;
		instanceTran.localPosition = Vector3.zero;
		instanceTran.localScale = Vector3.one;
		PanelManager.PanelModuleData panelData = PanelManager.GetInstance().getComponent(prefab);
		
		if(panelData == null)
		{
			Debug.Log("panelData is null!");
			return;
		}
		
		instance.name = prefab;
		string component = panelData.panelName;
		
		if (string.IsNullOrEmpty(component) == false)
		{
			UnityEngineInternal.APIUpdaterRuntimeServices.AddComponent(instance, "Assets/client_code/interface/manager/ResourceManager.cs (256,4)", component);
		}
		
		UIPanelBase panel = instance.gameObject.GetComponent<UIPanelBase>();
		if (instance != null && instance.gameObject != null)
		{
			instance.gameObject.SetActive(true);
		}
		
		
		GameUiManager.getInst().addLoadedWindow(prefab, panel, true);
		GameUiManager.getInst().removeLoadingWindow(prefab);
		Resources.UnloadUnusedAssets();
	}

	public void PreLoadAsync(string name)
	{
		if (isUseAB)
		{
			//string path = "file://" + System.IO.Path.Combine(Application.streamingAssetsPath, "DataIOS/" + type + "/" + name + ".ab");
//			LoadPrefabs(name, paramsList);
		}
		else
		{
			StartCoroutine(PreLoadPrefabFromRes(name));
		}
	}
	
	IEnumerator PreLoadPrefabFromRes(string name)
	{
		ResourceRequest request = Resources.LoadAsync("Prefabs/" + name);
		yield return request;
		GameObject instance = Instantiate(request.asset) as GameObject;
		
		Transform instanceTran = instance.transform;
		instanceTran.parent = GameUiManager.UIRootTran;
		instanceTran.localPosition = Vector3.zero;
		instanceTran.localScale = Vector3.one;
		
		PanelManager.PanelModuleData panelData = PanelManager.GetInstance().getComponent(name);

		
		instance.name = name;
		string component = panelData.panelName;
		
		if (string.IsNullOrEmpty(component) == false)
		{
			UnityEngineInternal.APIUpdaterRuntimeServices.AddComponent(instance, "Assets/client_code/interface/manager/ResourceManager.cs (341,4)", component);
		}
		
		UIPanelBase panel = instance.gameObject.GetComponent<UIPanelBase>();
		if (instance != null && instance.gameObject != null)
		{
			instance.gameObject.SetActive(false);
		}
		
		if (panel != null)
		{
			//GameUiManager.getInst().PreLoadDontUseThisShowPanel(panel);
		}
		
		GameUiManager.getInst().addLoadedWindow(name, panel, false);
		GameUiManager.getInst().removeLoadingWindow(name);
	}
}
