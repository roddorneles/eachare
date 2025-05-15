public interface MessageHandler {
    Message handle(Peer peer, Message message);
}
