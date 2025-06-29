public class LsMessage extends OutboundMessage {
    public LsMessage(Peer peer) {
        super(peer);
    }

    @Override
    public Message build() {
        return new Message(Message.Type.LS, senderIp, senderPort, clock);
    }

}
