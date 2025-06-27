import java.util.HashMap;
import java.util.Map;

public class MessageDispatcher {

    private final Map<Message.Type, MessageHandler> handlers = new HashMap<Message.Type, MessageHandler>();

    public MessageDispatcher() {
        handlers.put(Message.Type.HELLO, new HelloMessageHandler());
        handlers.put(Message.Type.GET_PEERS, new GetPeersMessageHandler());
        handlers.put(Message.Type.BYE, new GetPeersMessageHandler());
    }

    public Message dispatch(Peer peer, Message message) {
        MessageHandler handler = handlers.get(message.getType());
        if (handler != null) {
            return handler.handle(peer, message);
        }
        return null;
    }

}
