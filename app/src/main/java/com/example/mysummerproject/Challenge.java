package com.example.mysummerproject;

public class Challenge {
    private String id;
    private String title;
    private String tag;     // לדוגמה: "ריצה" או "כוח"
    private int points;     // לדוגמה: 500

    // בנאי ריק (חובה ל-Firebase)
    public Challenge() { }

    // בנאי מלא
    public Challenge(String id, String title, String tag, int points) {
        this.id = id;
        this.title = title;
        this.tag = tag;
        this.points = points;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }

    // Getters חדשים
    public String getTag() { return tag; }
    public int getPoints() { return points; }
}