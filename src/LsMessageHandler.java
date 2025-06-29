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

        if (peer.getSharedFolder().listFiles() != null || peer.getSharedFolder().listFiles().length > 0) {
            int amountSharedFiles = peer.getSharedFolder().listFiles().length;

            lsListMsg.addArg(String.valueOf(amountSharedFiles));

            for (int i = 0; i < amountSharedFiles; i += 1) {
                File file = peer.getSharedFolder().listFiles()[i];
                lsListMsg.addArg(String.format("%s:%d", file.getName(), file.length()));
            }
        }

        return lsListMsg;

    }
}
