public class ByeMessageHandler implements MessageHandler {

    @Override
    public Message handle(Peer peer, Message message) {

        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort(), "OFFLINE",
                message.getClock());

        System.out.println("[" + Thread.currentThread().getName() + "]" +
                String.format("Mensagem recebida: %s", message.toString()));

        peer.updateNeighbor(sender);

        return new Message(Message.Type.ACK, peer.getAddress(), peer.getPort(), peer.getClock());
    }

}
