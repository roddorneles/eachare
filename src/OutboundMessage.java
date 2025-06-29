import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public abstract class OutboundMessage {

    protected final Peer sender;
    protected final String senderIp;
    protected final int senderPort;
    protected int clock;

    public OutboundMessage(Peer sender) {
        this.sender = sender;
        this.senderIp = sender.getAddress();
        this.senderPort = sender.getPort();
        this.clock = sender.getClock();
    }

    // Cada tipo se constroi
    public abstract Message build();

    // Lógica de envio compartilhada
    public Message send(NeighborPeer receiver) {

        try {
            Socket socket = new Socket(receiver.getAddress(), receiver.getDoor());
            socket.setSoTimeout(2000);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // Aumenta-se o clock antes de enviar qualquer mensagem
            sender.increaseClock();
            this.clock = sender.getClock();

            Message message = this.build();

            System.out.printf("%s Enviando mensagem %s para %s%n", "[" + Thread.currentThread().getName() + "]",
                    message.toString(), receiver.getPeerName());

            oos.writeObject(message);
            oos.flush();

            // caso não ocorra nenhuma exceção após a criação do socket e o envio da
            // mensagem, atualiza-se o
            // estado do peer vizinho para online
            if ("OFFLINE".equals(receiver.getStatus())) {
                receiver.turnOn();
            }

            Message response = (Message) ois.readObject();

            // Se a mensagem de resposta não for um ACK, atualize-se o clock
            if (response.getType() != Message.Type.ACK) {
                System.out.printf("%s Resposta recebida: %s\n", "[" + Thread.currentThread().getName() + "]",
                        response.toString());
                sender.setClock(Math.max(this.clock, response.getClock()));
                sender.increaseClock();

                if (receiver.getClock() < response.getClock()) {
                    receiver.setClock(response.getClock());
                }
            }

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