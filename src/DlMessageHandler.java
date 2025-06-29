import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DlMessageHandler implements MessageHandler {
    @Override
    public Message handle(Peer peer, Message message) {

        NeighborPeer sender = new NeighborPeer(message.getSenderIp(), message.getSenderPort(), "ONLINE",
                message.getClock());
        peer.updateNeighbor(sender);

        Message fileMsg = new Message(Message.Type.FILE, peer.getAddress(), peer.getPort(), peer.getClock());

        String filename = message.getArgs().get(0);
        int number1 = Integer.parseInt(message.getArgs().get(1));
        int number2 = Integer.parseInt(message.getArgs().get(2));

        File targetFile = null;

        // Procurar o arquivo pelo nome
        for (File f : peer.getSharedFiles()) {
            if (f.getName().equals(filename)) {
                targetFile = f;
                break;
            }
        }

        if (targetFile == null) {
            System.out.println("Arquivo não encontrado.");
            return fileMsg;
        }

        try {
            // Leitura binária do arquivo
            FileInputStream fis = new FileInputStream(targetFile);
            byte[] fileBytes = new byte[(int) targetFile.length()];
            fis.read(fileBytes);
            fis.close();

            // Codificação Base64 (sem quebras de linha)
            String base64 = Base64.getEncoder().encodeToString(fileBytes);

            fileMsg.addArg(filename);
            fileMsg.addArg(String.valueOf(number1));
            fileMsg.addArg(String.valueOf(number2));
            fileMsg.addArg(base64);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileMsg;

    }
}
