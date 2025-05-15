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

        Message message = new Message(Message.Type.HELLO, this.address, this.door, this.clock);
        Connection conn = new Connection(message, peer);

        conn.send();

    }

    public void sendGetPeers() {

        for (NeighborPeer p : this.neighbors) {

            Message message = new Message(Message.Type.GET_PEERS, this.address, this.door, this.clock);
            Connection conn = new Connection(message, p);

            conn.send();
        }

    }

    private void receiveHello(Message message) {

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

    public void increaseClock() {
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
