package com.example.mysummerproject;

public class Challenge {
    private String id;
    private String title;

    // בנאי ריק (חובה ל-Firebase)
    public Challenge() { }

    // בנאי רגיל
    public Challenge(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
}
