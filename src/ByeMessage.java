public class ByeMessage extends OutboundMessage {
    public ByeMessage(Peer peer) {
        super(peer);
    }

    @Override
    public Message build() {
        return new Message(Message.Type.BYE, senderIp, senderPort, clock);
    }

}
