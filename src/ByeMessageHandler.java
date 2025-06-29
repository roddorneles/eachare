public class ByeMessageHandler implements MessageHandler {

    @Override
    public Message handle(Peer peer, Message message) {

        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort(), "OFFLINE",
                message.getClock());

        System.out.println("[" + Thread.currentThread().getName() + "]" +
                String.format("Mensagem recebida: %s", message.toString()));

        int neighboorIndex = peer.getNeighbors().indexOf(sender);

        // se encontra o peer na lista de vizinhos, atualiza o estado dele para OFFLINE.
        // Caso contrário, adiciona ele mas também com estado de OFFLINE
        // if (neighboorIndex != -1) {
        // sender.turnOff();
        // } else {
        // peer.addNeighbor(sender);
        // }

        peer.updateNeighbor(sender);

        return new Message(Message.Type.ACK, peer.getAddress(), peer.getPort(), peer.getClock());
    }

}
