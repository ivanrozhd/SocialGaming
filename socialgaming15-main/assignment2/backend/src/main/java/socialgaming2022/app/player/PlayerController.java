package socialgaming2022.app.player;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "player")
public class PlayerController {

    private final PlayerRepository playerRepository;


    @Autowired
    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @PostMapping
    public Player createPlayer(@RequestBody Player player) {
        Player newPLayer = new Player(player.getId(), player.getFirebaseUID(), player.getNickname(), player.getFriendsFirebaseUIDs());
        // convert Player in JSON and Insert in Database
        playerRepository.insert(newPLayer);
        return newPLayer;
    }

    @GetMapping(path = "{firebaseUID}")
    public Player findPlayerByFirebaseUID(@PathVariable String firebaseUID) {
        return playerRepository.findPlayerByFirebaseUID(firebaseUID)
                .orElseThrow(() -> new IllegalStateException("Error in findPlayerByFirebaseUID: No player with this firebaseUID '" + firebaseUID + "' in MongoDb database!"));
    }

    @GetMapping(path = "nickname/{name_part}")
    public List<Player> findPlayerByNicknameContaining(@PathVariable String name_part) {
        return playerRepository.findPlayersByNicknameContainingIgnoreCase(name_part);
    }

    @GetMapping(path = "uid/{uid_prefix}")
    public List<Player> findPlayersByFirebaseUIDBefore(@PathVariable String uid_prefix) {
        return playerRepository.findPlayersByFirebaseUIDStartingWith(uid_prefix);
    }

    @GetMapping(path = "friends/{uid}")
    public List<Player> getFriendsOf(@PathVariable String uid) {
        Player p = findPlayerByFirebaseUID(uid);
        ArrayList<String> friendUIDs = (ArrayList<String>) p.getFriendsFirebaseUIDs();
        ArrayList<Player> result = new ArrayList<>();
        for (String friend : friendUIDs) {
            result.add(findPlayerByFirebaseUID(friend));
        }
        return result;
    }

    @GetMapping(path = "friendRequests/{uid}")
    public List<Player> getFriendRequestsOf(@PathVariable String uid) {
        Player p = findPlayerByFirebaseUID(uid);
        ArrayList<String> friendRequestUIDs = (ArrayList<String>) p.getFriendRequestsUIDs();
        ArrayList<Player> result = new ArrayList<>();
        for (String friend : friendRequestUIDs) {
            result.add(findPlayerByFirebaseUID(friend));
        }
        return result;
    }

    @GetMapping
    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    @GetMapping(path="rank")
    public List<Player> findAllByOrderByLevelDesc() {
        return playerRepository.findAllByOrderByLevelDesc();
    }

    @PutMapping(path = "{firebaseUID}")
    public void updatePlayer(@PathVariable String firebaseUID,@RequestBody Player player) {
        Optional<Player> optional = playerRepository.findPlayerByFirebaseUID(firebaseUID);
        if (optional.isEmpty()) {
            System.err.println("No user with firebaseUID: " + firebaseUID);
            return;
        }
        Player updatedPlayer = optional.get();
        updatedPlayer.copyFrom(player);

        playerRepository.save(updatedPlayer);
    }// could return a response ResponseEntity<>(HttpStatus.NOT_FOUND)/(playerRepository.save(updatedPlayer), HttpStatus.OK)
}
