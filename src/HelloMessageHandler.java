public class HelloMessageHandler implements MessageHandler {
    @Override
    public Message handle(Peer peer, Message message) {
        // Cria um peer vizinho que está associado a mensagem que ele recebeu
        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort(), "ONLINE",
                message.getClock());
        peer.updateNeighbor(sender);

        // System.out.println("[" + Thread.currentThread().getName() + "]" +
        // String.format("Mensagem recebida: %s", message.toString()));

        return new Message(Message.Type.ACK, peer.getAddress(), peer.getPort(), peer.getClock());

    }
}
