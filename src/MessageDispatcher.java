import java.util.HashMap;
import java.util.Map;

public class MessageDispatcher {

    private final Map<Message.Type, MessageHandler> handlers = new HashMap<Message.Type, MessageHandler>();

    public MessageDispatcher() {
        handlers.put(Message.Type.HELLO, new HelloMessageHandler());
        handlers.put(Message.Type.GET_PEERS, new GetPeersMessageHandler());
        handlers.put(Message.Type.BYE, new ByeMessageHandler());
        handlers.put(Message.Type.LS, new LsMessageHandler());
        handlers.put(Message.Type.DL, new DlMessageHandler());
    }

    public Message dispatch(Peer peer, Message message) {
        MessageHandler handler = handlers.get(message.getType());
        if (handler != null) {

            System.out.println("[" + Thread.currentThread().getName() + "]" +
                    String.format("Mensagem recebida: %s", message.toString()));

            return handler.handle(peer, message);
        }
        return null;
    }

}
