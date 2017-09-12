using UnityEngine;
using System.Collections;

public class UIPanelBase : MonoBehaviour {
    public virtual void OnShow(params object[] paramsList) { }
	public virtual void OnClose(){}
	public virtual void OnRemove(){}
	public virtual void CloseSelf() 
	{
		GameUiManager.getInst().showWindow(name, false);
	}

	public virtual void GuidSystemOpenFunc(int msg){}
}