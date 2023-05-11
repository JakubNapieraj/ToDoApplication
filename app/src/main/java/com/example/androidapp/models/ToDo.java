package com.example.androidapp.models;

import java.io.Serializable;

public class ToDo implements Serializable {

    public String task, due, userId;
    public int status;

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isPinned;
}
