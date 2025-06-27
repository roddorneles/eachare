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
    private boolean isOnline = true;

    public Peer(String address, String door) {
        this.address = address;
        this.neighbors = new ArrayList<NeighborPeer>();
        this.door = Integer.parseInt(door);
        this.status = "OFFLINE";
        this.clock = 0;
        this.isOnline = true;

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

                }

            }

        }

    }

    public void sendBye() {

        for (NeighborPeer p : this.neighbors) {

            if (p.getStatus().equals("ONLINE")) {
                this.increaseClock();
                ByeMessage message = new ByeMessage(this.address, this.door, this.clock);

                message.send(p);
            }

        }

        this.isOnline = false;

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
            while (this.isOnline) {

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

                    // ao receber uma mensagem, aumenta-se o clock
                    this.increaseClock();
                    // envia a mensagem para o handler responsável
                    Message response = dispatcher.dispatch(this, received);

                    // envia a resposta do handler para o remetente
                    if (response != null) {
                        if (response.getType() != Message.Type.ACK) {
                            // ao enviar uma mensagem que não seja ACK para o remetente, aumenta-se o Clock
                            this.increaseClock();
                        }
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
