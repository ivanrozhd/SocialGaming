package socialgaming2022.app;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessagingService {
    private final FirebaseMessaging firebaseMessaging;

    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public String sendMessage(String token, String text) throws FirebaseMessagingException {
        Notification notification = Notification.builder().setTitle("TestMessage").setBody(text).build();
        Message message = Message.builder().setToken(token).setNotification(notification)
                .putData("tradingID", "-N5uNSdPABUsuKo8xZiE")
                .putData("requesterName", "<Test Name>")
                .build();
        return firebaseMessaging.send(message);
    }
}
