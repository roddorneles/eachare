public class HelloMessageHandler implements MessageHandler {
    @Override
    public Message handle(Peer peer, Message message) {
        // Cria um peer vizinho que está associado a mensagem que ele recebeu
        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort(), "ONLINE");

        System.out.println("[" + Thread.currentThread().getName() + "]" +
                String.format("Mensagem recebida: %s", message.toString()));

        // caso o peer remetente não esteja na lista de peer vizinhos, ele é adicionado
        // na lista de peers conhecidos
        // caso contrário, procuramos ele na lista de peers já conhecidos e atualizamos
        // o estado dele

        peer.addNeighbor(sender);

        // if (!peer.getNeighbors().contains(sender)) {
        // sender.turnOn();
        // peer.addNeighbor(sender);
        // System.out.println("[" + Thread.currentThread().getName() + "]"
        // + String.format("Adicionando peer %s status ONLINE", sender.getPeerName()));
        // } else {
        // for (NeighborPeer p : peer.getNeighbors()) {
        // if (p.equals(sender)) {
        // if (p.getStatus() == "OFFLINE") {
        // p.turnOn();
        // System.out.println("[" + Thread.currentThread().getName() + "]"
        // + String.format("Atualizando peer %s status ONLINE", sender.getPeerName()));
        // }
        // break;
        // }
        // }
        // }

        return new Message(Message.Type.ACK, peer.getAddress(), peer.getPort(), peer.getClock());

    }
}
