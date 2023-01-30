package socialgaming2022.app.player;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {
    Optional<Player> findPlayerByFirebaseUID(String firebaseUID);

    List<Player> findPlayersByNicknameContainingIgnoreCase(String name_part);

    List<Player> findPlayersByFirebaseUIDStartingWith(String uid_prefix);

    List<Player> findAllByOrderByLevelDesc();
}
