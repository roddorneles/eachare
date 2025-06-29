import java.util.ArrayList;
import java.util.List;

public class GetPeersMessageHandler implements MessageHandler {
    @Override
    public Message handle(Peer peer, Message message) {

        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort(), "ONLINE",
                message.getClock());
        peer.updateNeighbor(sender);

        Message listPeersMsg = new Message(Message.Type.PEER_LIST, peer.getAddress(), peer.getPort(), peer.getClock());

        // encontra todos os vizinhos que não seja o remetente e adiciona eles numa
        // lista separadas
        List<NeighborPeer> listPeersNeighbors = new ArrayList<NeighborPeer>();
        for (NeighborPeer p : peer.getNeighbors()) {
            if (!p.equals(sender)) {
                listPeersNeighbors.add(p);
            }
        }

        listPeersMsg.addArg(String.valueOf(listPeersNeighbors.size()));

        for (NeighborPeer p : listPeersNeighbors) {
            listPeersMsg.addArg(p.fullInfo());
        }

        return listPeersMsg;

    }
}
