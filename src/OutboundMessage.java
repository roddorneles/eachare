import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public abstract class OutboundMessage {

    protected final String senderIp;
    protected final int senderPort;
    protected final int clock;

    public OutboundMessage(String senderIp, int senderPort, int clock) {
        this.senderIp = senderIp;
        this.senderPort = senderPort;
        this.clock = clock;
    }

    // Cada tipo se constroi
    public abstract Message build();

    // Lógica de envio compartilhada
    public Message send(NeighborPeer receiver) {
        Message message = build();
        try {
            Socket socket = new Socket(receiver.getAddress(), receiver.getDoor());
            socket.setSoTimeout(2000);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            System.out.printf("%s Enviando mensagem %s para %s%n", "[" + Thread.currentThread().getName() + "]",
                    message.toString(), receiver.getPeerName());

            oos.writeObject(message);
            oos.flush();

            // caso não ocorra nenhuma exceção após a criação do socket e o envio da
            // mensagem, atualiza-se o
            // estado do peer vizinho para online
            if ("OFFLINE".equals(receiver.getStatus())) {
                receiver.turnOn();
                // System.out.printf("%s Atualizando peer %s status ONLINE%n",
                // "[" + Thread.currentThread().getName() + "]", receiver.getPeerName());
            }

            Message response = (Message) ois.readObject();
            socket.close();

            return response;

        } catch (SocketTimeoutException | ConnectException e) {
            System.err.printf("Falha ao conectar com %s%n", receiver.getPeerName());
            receiver.turnOff();
        } catch (IOException | ClassNotFoundException e) {
            System.err.printf("Erro de comunicação com %s%n", receiver.getPeerName());
            receiver.turnOff();
        }

        return null;
    }
}