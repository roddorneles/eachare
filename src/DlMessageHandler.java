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
        int chunkSize = Integer.parseInt(message.getArgs().get(1));
        int index = Integer.parseInt(message.getArgs().get(2));

        File targetFile = null;

        // Procurar o arquivo pelo nome
        for (File f : peer.getSharedFolder().listFiles()) {
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
            long fileLength = targetFile.length();
            long offset = (long) index * chunkSize;

            // Se o índice for inválido, retorna sem conteúdo
            if (offset >= fileLength) {
                System.out.printf("Chunk %d fora do limite do arquivo %s%n", index, filename);
                return fileMsg;
            }

            int bytesToRead = (int) Math.min(chunkSize, fileLength - offset);
            byte[] chunkBytes = new byte[bytesToRead];

            FileInputStream fis = new FileInputStream(targetFile);
            fis.skip(offset); // pula para a posição do chunk
            fis.read(chunkBytes, 0, bytesToRead); // lê somente o pedaço
            fis.close();

            // Codificação Base64 do chunk
            String base64 = Base64.getEncoder().encodeToString(chunkBytes);

            // Adiciona informações à resposta
            fileMsg.addArg(filename);
            fileMsg.addArg(String.valueOf(chunkSize));
            fileMsg.addArg(String.valueOf(index));
            fileMsg.addArg(base64);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileMsg;

    }
}
