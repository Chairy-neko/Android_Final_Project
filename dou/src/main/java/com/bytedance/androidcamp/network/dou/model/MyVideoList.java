package com.bytedance.androidcamp.network.dou.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MyVideoList implements Serializable {
    public List<Video> mVideos = new ArrayList<>();
    private int position;

    public void MyVideoPlayer() {

    }

    public void setmVideos(List<Video> mVideos) {
        this.mVideos = mVideos;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return this.position;
    }
}
