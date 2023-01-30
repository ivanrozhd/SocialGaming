package socialgaming2022.app.player;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "Players")
public class Player {
    // json keys should always start with a lowercase letter (else the setter can't be found)
    // Universally identifier
    @Id
    private String id;
    private String firebaseUID;
    //Firebase Cloud Massaging Token
    private String fcmtoken;
    private String nickname;

    //Friends list
    private List<String> friendsFirebaseUIDs;

    private List<String> friendRequestsUIDs;

    private int level;
    private int health;
    private int mood;
    private int hunger;

    private int food;
    private int pills;
    private int pizza;

    private int book;

    private int toy;

    private int coins;

    private int points;
    private int reachable_points;

    public Player() {
        // Initialize everything with values that should not be possible,
        // that way it can be distinguished whether the value was changed by a put request
        this.id = null;
        this.firebaseUID = null;
        this.fcmtoken = null;
        this.nickname = null;
        this.friendsFirebaseUIDs = null;
        this.friendRequestsUIDs = null;
        this.level = -1;
        this.health = -1;
        this.mood = -1;
        this.hunger = -1;
        this.food = -1;
        this.pills = -1;
        this.pizza = -1;
        this.book = -1;
        this.coins = -1;
        this.toy = -1;
        this.points = -1;
        this.reachable_points = -1;
        this.time = -1;
    }
    private long time;
    public Player(String id, String firebaseUID, String nickname, List<String> friendsFirebaseUIDs) {

        this.id = id;
        this.firebaseUID = firebaseUID;
        this.friendRequestsUIDs = new ArrayList<>();
        this.fcmtoken = null;
        this.nickname = nickname;

        this.level = 0;
        this.health = 5;
        this.mood = 5;
        this.hunger = 5;
        this.pills = 20;
        this.pizza = 20;
        this.food = 20;
        this.book = 2;
        this.toy = 4;
        this.coins = 50;
        this.points = 0;
        this.reachable_points = 100;// TODO what are reachable points and are they different for each person?
        this.friendsFirebaseUIDs = friendsFirebaseUIDs;
        this.time = System.currentTimeMillis();
    }


    public void copyFrom(Player other) {
        // Values are only copied if they were specified in the put request
        if (other.firebaseUID != null)
            firebaseUID = other.firebaseUID;
        if (other.fcmtoken != null)
            fcmtoken = other.fcmtoken;
        if (other.nickname != null)
            nickname = other.nickname;
        if (other.friendsFirebaseUIDs != null)
            friendsFirebaseUIDs = other.friendsFirebaseUIDs;
        if (other.friendRequestsUIDs != null)
            friendRequestsUIDs = other.friendRequestsUIDs;
        if (other.level != -1)
            level = other.level;
        if (other.health != -1)
            health = other.health;
        if (other.mood != -1)
            mood = other.mood;
        if (other.hunger != -1)
            hunger = other.hunger;
        if (other.food != -1)
            food = other.food;
        if (other.pills != -1)
            pills = other.pills;
        if (other.pizza != -1)
            pizza = other.pizza;
        if (other.book != -1) {
            book = other.book;
        }
        if (other.toy != -1) {
            toy = other.toy;
        }
        if (other.coins != -1) {
            coins = other.coins;
        }
        if (other.points != -1)
            points = other.points;
        if (other.reachable_points != -1)
            reachable_points = other.reachable_points;
        if (other.time != -1) {
            time = other.time;
        }
    }
    public void setId(String id) {
        this.id = id;
    }

    public void setFirebaseUID(String firebaseUID) {
        this.firebaseUID = firebaseUID;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setFriendsFirebaseUIDs(List<String> friendsFirebaseUIDs) {
        this.friendsFirebaseUIDs = friendsFirebaseUIDs;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public void setMood(int mood) {
        this.mood = mood;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public void setPills(int pills) {
        this.pills = pills;
    }

    public void setPizza(int pizza) {
        this.pizza = pizza;
    }

    public void setBook(int book) {
        this.book = book;
    }

    public void setToy(int toy) {
        this.toy = toy;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getFirebaseUID() {
        return firebaseUID;
    }

    public String getNickname() {
        return nickname;
    }

    public List<String> getFriendsFirebaseUIDs() {
        return friendsFirebaseUIDs;
    }

    public List<String> getFriendRequestsUIDs() {
        return friendRequestsUIDs;
    }

    public int getLevel() {
        return level;
    }

    public int getHealth() {
        return health;
    }

    public int getHunger() {
        return hunger;
    }

    public int getMood() {
        return mood;
    }

    public int getFood() {
        return food;
    }

    public int getPills() {
        return pills;
    }

    public int getPizza() {
        return pizza;
    }

    public String getFcmtoken() {
        return fcmtoken;
    }

    public void setFcmtoken(String fcmtoken) {
        this.fcmtoken = fcmtoken;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", firebaseUID='" + firebaseUID + '\'' +
                ", fcmtoken='" + fcmtoken + '\'' +
                ", nickname='" + nickname + '\'' +
                ", friendsFirebaseUIDs=" + friendsFirebaseUIDs +
                ", friendRequestUIDs=" + friendRequestsUIDs +
                ", level=" + level +
                ", health=" + health +
                ", mood=" + mood +
                ", hunger=" + hunger +
                ", food=" + food +
                ", pills=" + pills +
                ", pizza=" + pizza +
                ", points=" + points +
                ", reachable_points=" + reachable_points +
                ", time=" + time +
                '}';
    }
    public int getBook() {
        return book;
    }

    public int getToy() {
        return toy;
    }

    public int getCoins() {
        return coins;
    }

    public long getTime() {
        return time;
    }

    public int getPoints() {
        return points;
    }

    public int getReachable_points() {
        return reachable_points;
    }
}
