public class HelloMessage extends OutboundMessage {

    public HelloMessage(Peer peer) {
        super(peer);
    }

    @Override
    public Message build() {
        return new Message(Message.Type.HELLO, senderIp, senderPort, clock);
    }
}
