using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class VoiceManager : MonoBehaviour {

    public AudioSource mAudioMgr;

    private string mCurMusicName = "world01";

    private static VoiceManager instance;


    public static VoiceManager Instance
    {
        get
        {
            if (instance == null)
            {
                string objName = "VoiceManager";
                GameObject audioMgr = UnityEngine.GameObject.Find(objName);
                instance = audioMgr.GetComponent<VoiceManager>();
            }
            return instance;
        }
    }

    public void PlayMusic(string musicName)
    {
        this.PlayMusic(musicName, 0.6f);
    }

    public void PlayMusic(string musicName, float volume)
    {
        if (this.mAudioMgr.clip == null)
        {
            this.mAudioMgr.gameObject.GetComponent<AudioSource>().clip = Resources.Load("Audio/" + musicName) as AudioClip;
            this.mAudioMgr.volume = volume;
            this.mAudioMgr.loop = true;
            this.mAudioMgr.Play();
        }
        else
        {
            this.mAudioMgr.volume = volume;
            this.mAudioMgr.loop = true;
            if (!musicName.Equals(this.mCurMusicName))
            {
                AudioClip curClip = Resources.Load("Audio/" + musicName) as AudioClip;
                this.mCurMusicName = musicName;
                this.mAudioMgr.volume = volume;
                this.mAudioMgr.loop = true;
                this.mAudioMgr.clip = curClip;
                this.mAudioMgr.Play();
            }
        }
    }
}
