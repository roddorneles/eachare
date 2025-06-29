public class DlMessage extends OutboundMessage {

    private String filename;
    private int localChunk;
    private int index;

    public DlMessage(Peer peer, String filename, int localChunk, int index) {
        super(peer);
        this.filename = filename;
        this.localChunk = localChunk;
        this.index = index;
    }

    @Override
    public Message build() {
        Message message = new Message(Message.Type.DL, senderIp, senderPort, clock);
        message.addArg(filename);
        message.addArg(String.valueOf(localChunk));
        message.addArg(String.valueOf(index));
        return message;
    }

}
