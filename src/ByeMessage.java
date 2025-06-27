public class ByeMessage extends OutboundMessage {
    public ByeMessage(String senderIp, int senderPort, int clock) {
        super(senderIp, senderPort, clock);
    }

    @Override
    public Message build() {
        return new Message(Message.Type.BYE, senderIp, senderPort, clock);
    }

}
