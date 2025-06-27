public class GetPeersMessage extends OutboundMessage {

    public GetPeersMessage(String senderIp, int senderPort, int clock) {
        super(senderIp, senderPort, clock);
    }

    @Override
    public Message build() {
        return new Message(Message.Type.GET_PEERS, senderIp, senderPort, clock);
    }

}