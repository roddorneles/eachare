import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Peer extends Thread {

    public ServerSocket server;
    private String status;
    private String address;
    private int door;
    private int clock;
    private List<NeighborPeer> neighbors;
    private MessageDispatcher dispatcher = new MessageDispatcher();

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

        this.increaseClock();
        HelloMessage helloMessage = new HelloMessage(this.address, this.door, this.clock);
        helloMessage.send(peer);

    }

    public void sendGetPeers() {

        List<NeighborPeer> peersToBeAdded = new ArrayList<NeighborPeer>();

        List<NeighborPeer> knownNeighbors = new ArrayList<>(this.neighbors);

        for (NeighborPeer p : knownNeighbors) {

            this.increaseClock();
            GetPeersMessage message = new GetPeersMessage(this.address, this.door, this.clock);

            Message response = message.send(p);

            if (response != null) {

                System.out.printf("%s Resposta recebida: %s\n", "[" + Thread.currentThread().getName() + "]",
                        response.toString());
                this.increaseClock();

                int numberOfNeighbors = Integer.parseInt(response.getArgs().get(0));

                for (int i = 1; i <= numberOfNeighbors; i += 1) {

                    String neighboor = response.getArgs().get(i);

                    String[] neighboorSplited = neighboor.split(":");
                    String address = neighboorSplited[0];
                    int port = Integer.parseInt(neighboorSplited[1]);
                    String status = neighboorSplited[2];

                    NeighborPeer newNeighbor = new NeighborPeer(address, port, status);

                    this.addNeighbor(newNeighbor);
                    // peersToBeAdded.add(newNeighbor);

                }

            }

        }

        // adiciona os peers descobertos na lista de vizinhos de peers
        // for (NeighborPeer p : peersToBeAdded) {
        // this.addNeighbor(p);
        // }

    }

    public void increaseClock() {
        this.clock += 1;
        System.out.println("[" + Thread.currentThread().getName() + "]"
                + String.format("=> Atualizando relogio para %d", this.clock));
    }

    public void addNeighbor(NeighborPeer p) {

        int index = this.neighbors.indexOf(p);

        // se encontrou aquele peer na lista de vizinhos, atualiza o estado. Se não,
        // adiciona na lista de vizinhos
        if (index != -1) {
            NeighborPeer neighbor = this.neighbors.get(index);
            if (p.getStatus().equals("ONLINE")) {
                neighbor.turnOn();
            } else if (p.getStatus().equals("OFFLINE")) {
                neighbor.turnOff();
            }

        } else {
            this.neighbors.add(p);
            System.out.println("[" + Thread.currentThread().getName() + "]"
                    + String.format("Adicionando novo peer %s:%d status %s", p.getAddress(), p.getDoor(),
                            p.getStatus()));
        }

    }

    public List<NeighborPeer> getNeighbors() {
        return this.neighbors;
    }

    public String getPeerName() {
        return String.format("%s:%s", this.address, this.door);
    }

    public int getClock() {
        return this.clock;
    }

    public int getPort() {
        return this.door;
    }

    public String getAddress() {
        return this.address;
    }

    // Função da thread a ser executadas emoutra porta para simular peer
    @Override
    public void run() {
        try {
            while (true) {

                // aguardando conexão na porta do servidor
                Socket client = this.server.accept();
                // ao receber uma mensagem, aumenta-se o clock
                this.increaseClock();
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

                Message received = null;

                try {
                    received = (Message) ois.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (received != null) {

                    // envia a mensagem para o handler responsável
                    Message response = dispatcher.dispatch(this, received);

                    // envia a resposta do handler para o remetente
                    if (response != null) {
                        oos.writeObject(response);
                        oos.flush();
                    }

                }

                client.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
