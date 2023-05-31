package com.example.androidapp.models;

import android.net.Uri;

import java.io.Serializable;

public class ToDo implements Serializable {

    public String task, due, userId;
    public Uri imageURI;
    public int status;

}
