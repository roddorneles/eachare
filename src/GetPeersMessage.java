public class GetPeersMessage extends OutboundMessage {

    public GetPeersMessage(Peer peer) {
        super(peer);
    }

    @Override
    public Message build() {
        return new Message(Message.Type.GET_PEERS, senderIp, senderPort, clock);
    }

}