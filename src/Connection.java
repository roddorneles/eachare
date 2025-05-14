import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Connection {

    private Message message;
    private NeighborPeer receiver;
    private Socket client;

    public Connection(Message message, NeighborPeer receiver) {
        this.message = message;
        this.receiver = receiver;
    }

    public void send() {

        try {
            // cria a conexão com o destinatário
            Socket client = new Socket(receiver.getAddress(), receiver.getDoor());
            client.setSoTimeout(2000);

            System.out.println(
                    String.format(" Encaminhando mensagem %s para %s\r\n", message.toString(), receiver.getPeerName()));

            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

            oos.writeObject(message);
            oos.flush();

            try {
                // aguarda a resposta do destinatário para confirmar que ele recebeu de fato a
                // mensagem
                Message response = (Message) ois.readObject();
                if (receiver.getStatus() == "OFFLINE") {
                    System.out.println(String.format("Atualizando peer %s status ONLINE", receiver.getPeerName()));
                    receiver.turnOn();
                }
            } catch (SocketTimeoutException e) {
                System.err.println("Timeout ao tentar comunicar com " + receiver.getPeerName());
                System.out.println(String.format("Atualizando peer %s status OFFLINE", receiver.getPeerName()));
                receiver.turnOff();
            } catch (ConnectException e) {
                System.err.println("Peer recusou a conexão: " + receiver.getPeerName());
                System.out.println(String.format("Atualizando peer %s status OFFLINE", receiver.getPeerName()));
                receiver.turnOff();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro de comunicação com " + receiver.getPeerName());
                System.out.println(String.format("Atualizando peer %s status OFFLINE", receiver.getPeerName()));
                receiver.turnOff();
            }

            client.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
