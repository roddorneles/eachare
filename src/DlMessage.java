public class DlMessage extends OutboundMessage {

    private String filename;

    public DlMessage(Peer peer, String filename) {
        super(peer);
        this.filename = filename;
    }

    @Override
    public Message build() {
        Message message = new Message(Message.Type.DL, senderIp, senderPort, clock);
        message.addArg(filename);
        message.addArg("0");
        message.addArg("0");
        return message;
    }

}
