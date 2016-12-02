package com.chipcerio.tambayan.model.pojo;

import com.google.gson.annotations.Expose;

public class Category {

    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
