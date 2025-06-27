public class HelloMessage extends OutboundMessage {

    public HelloMessage(String senderIp, int senderPort, int clock) {
        super(senderIp, senderPort, clock);
    }

    @Override
    public Message build() {
        return new Message(Message.Type.HELLO, senderIp, senderPort, clock);
    }
}
