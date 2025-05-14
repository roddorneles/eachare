import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Peer extends Thread {

    public ServerSocket server;
    private String status;
    private String address;
    private int door;
    private int clock;
    private List<NeighborPeer> neighbors;

    public Peer(String address, String door) {
        this.address = address;
        this.neighbors = new ArrayList<NeighborPeer>();
        this.door = Integer.parseInt(door);
        this.status = "OFFLINE";
        this.clock = 0;

        try {
            this.server = new ServerSocket(this.door);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHello(NeighborPeer peer) {

        // Message message = new Message(Message.Type.HELLO, this.address, this.door, this.clock);
        // Connection conn = new Connection(message, peer);

        try {
            // cria a conexão com o destinatário
            Socket client = new Socket(peer.getAddress(), peer.getDoor());
            client.setSoTimeout(2000);

            this.increaseClock();

            // cria a mensagem a ser enviada e a envia para o destinatário
            Message message = new Message(Message.Type.HELLO, this.address, this.door, this.clock);
            System.out.println(
                    String.format(" Encaminhando mensagem %s para %s\r\n", message.format(), peer.getPeerName()));

            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

            oos.writeObject(message);
            oos.flush();

            try {
                // aguarda a resposta do destinatário para confirmar que ele recebeu de fato a
                // mensagem
                Message response = (Message) ois.readObject();
                if (peer.getStatus() == "OFFLINE") {
                    System.out.println(String.format("Atualizando peer %s status ONLINE", peer.getPeerName()));
                    peer.turnOn();
                }
            } catch (SocketTimeoutException e) {
                System.err.println("Timeout ao tentar comunicar com " + peer.getPeerName());
                peer.turnOff();
            } catch (ConnectException e) {
                System.err.println("Peer recusou a conexão: " + peer.getPeerName());
                peer.turnOff();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro de comunicação com " + peer.getPeerName());
                peer.turnOff();
            }

            client.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendGetPeers() {

        for (NeighborPeer p : this.neighbors) {
            Socket client;
            try {
                client = new Socket(p.getAddress(), p.getDoor());
                client.setSoTimeout(2000);

                this.increaseClock();

                Message getPeersMsg = new Message(Message.Type.GET_PEERS, this.address, this.door, this.clock);

                System.out.println(String.format(" Encaminhando mensagem %s para %s", getPeersMsg.toString(), p.getPeerName()));

                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

                oos.writeObject(getPeersMsg);
                oos.flush();

                try {
                    // aguarda a resposta do destinatário para confirmar que ele recebeu de fato a
                    // mensagem
                    Message response = (Message) ois.readObject();
                    System.out.println("respondeu!!");
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout ao tentar comunicar com " + p.getPeerName());
                    p.turnOff();
                } catch (ConnectException e) {
                    System.err.println("Peer recusou a conexão: " + p.getPeerName());
                    p.turnOff();
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Erro de comunicação com " + p.getPeerName());
                    p.turnOff();
                }

                client.close();

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void receiveHello(Message message) {

        // Cria um peer vizinho que está associado a mensagem que ele recebeu
        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort());

        this.increaseClock();

        System.out.println(String.format("Mensagem recebida: %s", message.format()));

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

    private Message receiveGetPeers(Message message) {

        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort());

        Message listPeersMsg = new Message(Message.Type.PEER_LIST, this.address, this.door, this.clock);

        // encontra todos os vizinhos que não seja o remetente
        List<NeighborPeer> listPeersNeighbors = new ArrayList<NeighborPeer>();
        for (NeighborPeer p : neighbors) {
            if (!p.equals(sender)) {
                listPeersNeighbors.add(p);
            }
        }

        listPeersMsg.addArg(String.valueOf(listPeersNeighbors.size()));

        for (NeighborPeer p : listPeersNeighbors) {
            listPeersMsg.addArg(p.fullInfo());
        }

        System.out.println(listPeersMsg.toString());

        return listPeersMsg;

    }

    private void increaseClock() {
        this.clock += 1;
        System.out.println(String.format("=> Atualizando relogio para %d", this.clock));
    }

    public void addNeighbor(NeighborPeer p) {
        if (!this.neighbors.contains(p)) {
            this.neighbors.add(p);
        }
    }

    public List<NeighborPeer> getNeighbors() {
        return this.neighbors;
    }

    public String getPeerName() {
        return String.format("%s:%s", this.address, this.door);
    }

    // Função da thread a ser executadas emoutra porta para simular peer
    @Override
    public void run() {
        try {
            while (true) {

                // aguardando conexão na porta do servidor
                Socket client = this.server.accept();

                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

                Message received = null;

                try {
                    received = (Message) ois.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (received != null) {
                    switch (received.getType()) {
                        case HELLO:
                            receiveHello(received);
                            Message ack = new Message(Message.Type.ACK, this.address, this.door, this.clock);
                            oos.writeObject(ack);
                            oos.flush();
                            break;

                        case GET_PEERS:
                            receiveGetPeers(received);
                            break;

                        default:
                            break;
                    }
                }

                client.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
