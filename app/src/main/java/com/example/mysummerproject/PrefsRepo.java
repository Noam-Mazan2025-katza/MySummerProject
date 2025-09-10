package com.example.mysummerproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public class PrefsRepo {
    private static final String PREFS = "prefs_repo";
    private static final String KEY_USERS = "users_json";

    public static class User {
        public String name;
        public String avatarUri; // נשמר כמחרוזת
        public int points;
        public boolean badge;
        public User(String name, String avatarUri, int points, boolean badge) {
            this.name = name; this.avatarUri = avatarUri; this.points = points; this.badge = badge;
        }
    }

    public static List<User> getUsers(Context ctx) {
        ArrayList<User> list = new ArrayList<>();
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_USERS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i=0;i<arr.length();i++){
                JSONObject o = arr.getJSONObject(i);
                list.add(new User(
                        o.optString("name",""),
                        o.optString("avatarUri", null),
                        o.optInt("points",0),
                        o.optBoolean("badge",false)
                ));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    public static List<User> getUsersSorted(Context ctx) {
        List<User> list = getUsers(ctx);
        Collections.sort(list, (a,b)->Integer.compare(b.points, a.points));
        return list;
    }

    public static void addUser(Context ctx, String name, Uri avatar) {
        List<User> users = getUsers(ctx);
        users.add(new User(name, avatar!=null?avatar.toString():null, 0, false));
        saveUsers(ctx, users);
    }

    public static void addPoints(Context ctx, String name, int pts) {
        List<User> users = getUsers(ctx);
        for (User u: users) if (u.name.equals(name)) { u.points += pts; break; }
        saveUsers(ctx, users);
    }

    public static void setBadge(Context ctx, String name, boolean val) {
        List<User> users = getUsers(ctx);
        for (User u: users) if (u.name.equals(name)) { u.badge = val; break; }
        saveUsers(ctx, users);
    }

    public static List<String> getUserNames(Context ctx){
        List<String> names = new ArrayList<>();
        for (User u: getUsers(ctx)) names.add(u.name);
        return names;
    }

    private static void saveUsers(Context ctx, List<User> users){
        JSONArray arr = new JSONArray();
        try {
            for (User u: users){
                JSONObject o = new JSONObject();
                o.put("name", u.name);
                if (u.avatarUri != null) o.put("avatarUri", u.avatarUri); else o.put("avatarUri", JSONObject.NULL);
                o.put("points", u.points);
                o.put("badge", u.badge);
                arr.put(o);
            }
        } catch (JSONException e){ e.printStackTrace(); }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_USERS, arr.toString()).apply();
    }
    public static void removeUsers(Context ctx, java.util.Collection<String> names) {
        java.util.List<User> users = getUsers(ctx);        // טען את הרשימה מה-SharedPreferences
        java.util.Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (names.contains(u.name)) {
                it.remove();
            }
        }
        saveUsers(ctx, users);                              // חשוב: לשמור חזרה!
    }


}

