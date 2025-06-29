import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LsMessageHandler implements MessageHandler {
    @Override
    public Message handle(Peer peer, Message message) {

        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort(), "ONLINE",
                message.getClock());
        peer.updateNeighbor(sender);

        Message lsListMsg = new Message(Message.Type.LS_LIST, peer.getAddress(), peer.getPort(), peer.getClock());

        if (peer.getSharedFiles() != null || peer.getSharedFiles().length > 0) {
            int amountSharedFiles = peer.getSharedFiles().length;

            lsListMsg.addArg(String.valueOf(amountSharedFiles));

            for (int i = 0; i < amountSharedFiles; i += 1) {
                File file = peer.getSharedFiles()[i];
                lsListMsg.addArg(String.format("%s:%d", file.getName(), file.length()));
            }
        }

        return lsListMsg;

    }
}
