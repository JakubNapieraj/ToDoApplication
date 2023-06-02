package com.example.androidapp.models;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class ToDo implements Serializable {

    public String task, due, userId;
    public Bitmap taskImage;
    public int status;

}
