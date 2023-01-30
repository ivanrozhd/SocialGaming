package com.example.littlelarri;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private static final String TAG = "Player";

    private String UID;
    private String nickname;
    private List<String> friends;
    private List<String> friendRequests;

    private float level;

    private int[] characteristics; // hunger, health, mood
    private int[] resources; // water, food, pills, toys, books, coins

    private boolean isCurrentUser;

    public Player(String UID, String nickname, List<String> friends, List<String> friendRequests, float level, int[] characteristics, int[] resources, boolean isCurrentUser) {
        this.UID = UID;
        this.nickname = nickname;
        this.friends = friends;
        this.friendRequests = friendRequests;
        this.level = level;

        // check that array parameters have right length

        if (characteristics.length != 3) {
            this.characteristics = new int[3];
            Log.w(TAG, "You can only create a player with 3 characteristics, you gave " + characteristics.length);
        } else {
            this.characteristics = characteristics;
        }

        if (resources.length != 6) {
            this.resources = new int[6];
            Log.w(TAG, "You can only create a player with 5 resources, you gave " + resources.length);
        } else {
            this.resources = resources;
        }

        this.isCurrentUser = isCurrentUser;
    }

    public Player(String UID, String nickname) {
        this(UID, nickname, new ArrayList<>(), new ArrayList<>(), 0, new int[3], new int[6], false);
    }

    public static Player withUID(String UID) {
        return null;
    }

    // Setter ------------------------------------------------------------------------------------
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setLevel(float level) {
        this.level = level;
    }
    public void addFriend(String player) {
        friends.add(player);
    }
    public boolean removeFriend(String player) {
        return friends.remove(player);
    }
    public void addFriendRequest(String uid) {
        friendRequests.add(uid);
    }
    public boolean removeFriendRequest(String uid) {
        return friendRequests.remove(uid);
    }
    public void setHunger(int hunger) {
        if (hunger > 100) hunger = 100;
        if (hunger < 0) hunger = 0;
        characteristics[0] = hunger;
    }
    public void setHealth(int health) {
        if (health > 100) health = 100;
        if (health < 0) health = 0;
        characteristics[1] = health;
    }
    public void setMood(int mood) {
        if (mood > 100) mood = 100;
        if (mood < 0) mood = 0;
        characteristics[2] = mood;
    }
    public void setWater(int water) {
        if (water < 0) water = 0;
        resources[0] = water;
    }
    public void setFood(int food) {
        if (food < 0) food = 0;
        resources[1] = food;
    }
    public void setPills(int pills) {
        if (pills < 0) pills = 0;
        resources[2] = pills;
    }
    public void setToys(int toys) {
        if (toys < 0) toys = 0;
        resources[3] = toys;
    }
    public void setBooks(int books) {
        if (books < 0) books = 0;
        resources[4] = books;
    }
    public void setCoins(int coins) {
        if (coins < 0) coins = 0;
        resources[5] = coins;
    }

    // Getter ------------------------------------------------------------------------------------
    public String getUID() {
        return UID;
    }
    public String getNickname() {
        return nickname;
    }
    public List<String> getFriends() {
        return friends;
    }
    public List<String> getFriendRequests() {
        return friendRequests;
    }
    public float getLevel() {
        return level;
    }
    public int getHunger() {
        return characteristics[0];
    }
    public int getHealth() {
        return characteristics[1];
    }
    public int getMood() {
        return characteristics[2];
    }
    public int getWater() {
        return resources[0];
    }
    public int getFood() {
        return resources[1];
    }
    public int getPills() {
        return resources[2];
    }
    public int getToys() {
        return resources[3];
    }
    public int getBooks() {
        return resources[4];
    }
    public int getCoins() {
        return resources[5];
    }
    public boolean isCurrentUser() {
        return isCurrentUser;
    }
}
