package socialgaming2022.app;

import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import socialgaming2022.app.player.Player;
import socialgaming2022.app.player.PlayerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TradingManager implements CommandLineRunner {
    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public void run(String... args) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://little-laerri-default-rtdb.europe-west1.firebasedatabase.app/");
        database.getReference().child("trading").addChildEventListener(new TradingChildEventListener(playerRepository));
    }

    private static class TradingChildEventListener implements ChildEventListener {
        // These constants also need to be updated in the frontend when changed
        public final static String ACTION_KEY = "action";
        public final static String ACTION_ACCEPTED = "accepted";
        public final static String ACTION_REJECTED = "rejected";
        public final static String ACTION_REQUESTED = "requested";
        public final static String ACTION_CANCELED = "canceled";
        public final static String ACTION_SCANNED = "scanned";
        public final static String ACTION_COMPLETED = "completed";
        public final static String ACTION_ERROR = "error";

        public final static int TRADING_ITEMS_AMOUNT = 6;
        public final static int TRADING_MOOD_INCREASE = 20;
        public final static int TRADING_FRIEND_BONUS = 5;


        private PlayerRepository repository;
        private FirebaseMessagingService service;

        public TradingChildEventListener(PlayerRepository playerRepository) {
            this.repository = playerRepository;
            service = new FirebaseMessagingService(FirebaseMessaging.getInstance());
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) dataSnapshot.getValue();// TODO: error handling
            Optional<Player> requester = repository.findPlayerByFirebaseUID((String) data.get("requester").get("firebaseUID"));
            Optional<Player> requested = repository.findPlayerByFirebaseUID((String) data.get("requested").get("firebaseUID"));

            String requesterName = requester.get().getNickname();
            String text = requesterName + " wants to trade with you.";// TODO: extract string?
            try {
                Notification notification = Notification.builder()
                        .setTitle("Trading")
                        .setBody(text)
                        .build();
                Message message = Message.builder()
                        .setToken(requested.get().getFcmtoken())
                        .setNotification(notification)
                        .putData("tradingID", dataSnapshot.getKey())
                        .putData("requesterName", requesterName)
                        .build();
                FirebaseMessaging.getInstance().send(message);
                System.out.println("Send message, tradingID: " + dataSnapshot.getKey());
            } catch (FirebaseMessagingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
            String action = (String) map.get(ACTION_KEY);
            if (action == null) {
                System.err.println("TradingManager: onChildChanged: action was null");
                return;
            }
            // Delete database entry if trade was completed
            if (action.equals(ACTION_CANCELED) || action.equals(ACTION_REJECTED) || action.equals(ACTION_ERROR))
                deleteTradingEntry(dataSnapshot.getKey());
            else if (action.equals(ACTION_COMPLETED)) {// If completed, then the items have to be exchanged
                System.out.println("Exchanged items of trade: " + dataSnapshot.getKey());

                UserTradeData userData0 = new UserTradeData(map.get("requester"));
                UserTradeData userData1 = new UserTradeData(map.get("requested"));

                Optional<Player> optionalPlayer0 = repository.findPlayerByFirebaseUID(userData0.getUserID());
                Optional<Player> optionalPlayer1 = repository.findPlayerByFirebaseUID(userData1.getUserID());
                if (optionalPlayer0.isPresent() && optionalPlayer1.isPresent()) {
                    // Swap items
                    userData0.addValuesToPlayer(optionalPlayer1.get());
                    userData0.subtractValuesFromPlayer(optionalPlayer0.get());
                    userData1.addValuesToPlayer(optionalPlayer0.get());
                    userData1.subtractValuesFromPlayer(optionalPlayer1.get());

                    //Increase mood of pets
                    boolean friends = areFriends(optionalPlayer0.get(), optionalPlayer1.get());
                    int moodIncrease  = TRADING_MOOD_INCREASE + (friends ? TRADING_FRIEND_BONUS : 0);
                    optionalPlayer0.get().setMood(optionalPlayer0.get().getMood() + moodIncrease);
                    optionalPlayer1.get().setMood(optionalPlayer1.get().getMood() + moodIncrease);

                    // Save changes
                    repository.save(optionalPlayer0.get());
                    repository.save(optionalPlayer1.get());
                }
                else
                    System.err.println("Error could not complete trade because one or both users could not be found in the database");
                // Delete trading database entry not needed anymore
                deleteTradingEntry(dataSnapshot.getKey());
            }
        }

        private static boolean areFriends(Player playerA, Player playerB) {
            String playerAUID = playerA.getFirebaseUID();
            for (String uid : playerB.getFriendsFirebaseUIDs())
                if (uid.equals(playerAUID))
                    return true;
            return false;
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {}
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
        @Override
        public void onCancelled(DatabaseError databaseError) {}

        private static void deleteTradingEntry(String tradingID) {
            FirebaseDatabase.getInstance("https://little-laerri-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference().child("trading").child(tradingID).removeValue(null);
        }

        private static class UserTradeData {
            private final String userID;
            private final List<Long> items;

            public UserTradeData(Map<String, Object> map) {
                userID = (String) map.get("firebaseUID");
                // Sometimes a list is passed and sometimes a map
                if (map.get("items") instanceof Map) {
                    Map<String, Long> itemsMap = (Map<String, Long>)map.get("items");
                    items = new ArrayList<>(TRADING_ITEMS_AMOUNT);
                    for (int i = 0; i < TRADING_ITEMS_AMOUNT; i++) {
                        Long item = itemsMap.get(i + "");
                        items.add(item == null ? 0 : item);
                    }
                }
                else
                    items = (List<Long>) map.get("items");
            }

            public UserTradeData(Object map) {
                this((Map<String, Object>) map);
            }

            public String getUserID() {
                return userID;
            }

            public List<Long> getItems() {
                return items;
            }

            public void addValuesToPlayer(Player player) {
                addValuesToPlayer(player, +1);
            }

            public void subtractValuesFromPlayer(Player player) {
                addValuesToPlayer(player, -1);
            }

            private void addValuesToPlayer(Player player, int sign) {
                int[] itemsArray = new int[TRADING_ITEMS_AMOUNT];
                int i = 0;
                for (Long value : items)
                    itemsArray[i++] = value == null ? 0 : value.intValue() * sign;
                // Needs to be synced with frontend
                player.setFood( player.getFood()+ itemsArray[0]);
                player.setPills(player.getPills()+itemsArray[1]);
                player.setPizza(player.getPizza()+itemsArray[2]);
                player.setBook( player.getBook()+ itemsArray[3]);
                player.setToy(  player.getToy()+  itemsArray[4]);
                player.setCoins(player.getCoins()+itemsArray[5]);
            }
        }
    }
}
