public class HelloMessageHandler implements MessageHandler {
    @Override
    public Message handle(Peer peer, Message message) {
        // Cria um peer vizinho que está associado a mensagem que ele recebeu
        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort());

        peer.increaseClock();

        System.out.println(String.format("Mensagem recebida: %s", message.toString()));

        // caso o peer remetente não esteja na lista de peer vizinhos, ele é adicionado
        // na lista de peers conhecidos
        // caso contrário, procuramos ele na lista de peers já conhecidos e atualizamos
        // o estado dele
        if (!this.neighbors.contains(sender)) {
            sender.turnOn();
            this.addNeighbor(sender);
            System.out.println(String.format("Atualizando peer %s status ONLINE", sender.getPeerName()));
        } else {
            for (NeighborPeer p : this.neighbors) {
                if (p.equals(sender)) {
                    if (p.getStatus() == "OFFLINE") {
                        p.turnOn();
                        System.out.println(String.format("Atualizando peer %s status ONLINE", sender.getPeerName()));
                    }
                    break;
                }
            }
        }
    }
}
