import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Peer extends Thread {

    public ServerSocket server;
    private String status;
    private String address;
    private int door;
    private int clock;
    private List<NeighborPeer> neighbors;
    private MessageDispatcher dispatcher = new MessageDispatcher();
    private boolean isOnline;
    private File folder;
    private int chunckSize;

    public Peer(String address, String door) {
        this.address = address;
        this.neighbors = new ArrayList<NeighborPeer>();
        this.door = Integer.parseInt(door);
        this.status = "OFFLINE";
        this.clock = 0;
        this.isOnline = true;
        this.chunckSize = 256;

        try {
            this.server = new ServerSocket(this.door);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHello(NeighborPeer peer) {

        HelloMessage helloMessage = new HelloMessage(this);
        helloMessage.send(peer);

    }

    public void sendGetPeers() {

        List<NeighborPeer> knownNeighbors = new ArrayList<>(this.neighbors);

        for (NeighborPeer p : knownNeighbors) {

            GetPeersMessage message = new GetPeersMessage(this);
            Message response = message.send(p);

            if (response != null) {

                int numberOfNeighbors = Integer.parseInt(response.getArgs().get(0));

                for (int i = 1; i <= numberOfNeighbors; i += 1) {

                    String neighboor = response.getArgs().get(i);

                    String[] neighboorSplited = neighboor.split(":");
                    String address = neighboorSplited[0];
                    int port = Integer.parseInt(neighboorSplited[1]);
                    String status = neighboorSplited[2];
                    int neighborClock = Integer.parseInt(neighboorSplited[3]);

                    NeighborPeer newNeighbor = new NeighborPeer(address, port, status, neighborClock);

                    this.updateNeighbor(newNeighbor);

                }
            }
        }
    }

    public Map<FoundFile, FoundFile> sendList() {

        List<Message> responses = new ArrayList<Message>();

        for (NeighborPeer p : this.neighbors) {
            if (p.getStatus().equals("ONLINE")) {
                LsMessage message = new LsMessage(this);
                Message response = message.send(p);

                responses.add(response);
            }
        }

        Map<FoundFile, FoundFile> foundFilesMap = new HashMap<>();

        for (Message response : responses) {
            int numberOfArgs = Integer.parseInt(response.getArgs().get(0));

            for (int i = 1; i <= numberOfArgs; i += 1) {

                String arg = response.getArgs().get(i);

                String[] argSplited = arg.split(":");
                String fileName = argSplited[0];
                int fileSize = Integer.parseInt(argSplited[1]);
                String address = response.getSenderIp();
                int port = response.getSenderPort();

                NeighborPeer peer = this.findNeighbor(address, port);

                FoundFile foundFile = new FoundFile(fileName, fileSize);

                if (foundFilesMap.containsKey(foundFile)) {
                    FoundFile existing = foundFilesMap.get(foundFile);
                    existing.addPeer(peer);
                } else {
                    // Não existe: adiciona o peer e insere no mapa
                    foundFile.addPeer(peer);
                    foundFilesMap.put(foundFile, foundFile);
                }

            }
        }

        return foundFilesMap;
    }

    public void sendDl(FoundFile file) {

        int fileSize = file.getSize();
        int chunkSize = this.chunckSize;
        int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);

        String[] base64Chunks = new String[totalChunks];

        List<Thread> threads = new ArrayList<>();

        for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
            int finalChunkIndex = chunkIndex;

            // Escolher um peer para esse chunk (distribuído de forma simples)
            NeighborPeer peer = file.getPeers().get(finalChunkIndex % file.getPeers().size());

            Thread thread = new Thread(() -> {
                DlMessage dlMessage = new DlMessage(this, file.getFilename(), chunkSize, finalChunkIndex);
                Message response = dlMessage.send(peer);

                if (response != null) {
                    String base64Content = response.getArgs().get(3);
                    base64Chunks[finalChunkIndex] = base64Content;
                } else {
                    System.err.printf("Chunk %d falhou com o peer %s%n", finalChunkIndex, peer.fullInfo());
                }
            });

            threads.add(thread);
            thread.start();
        }

        // Aguarda todas as threads finalizarem
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrompida.");
            }
        }

        // Verificar se todos os chunks foram recebidos
        for (int i = 0; i < base64Chunks.length; i++) {
            if (base64Chunks[i] == null) {
                System.err.printf("Erro: chunk %d não foi recebido.%n", i);
                return;
            }
        }

        // Juntar os chunks em um único array de bytes
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (String chunk : base64Chunks) {
                byte[] chunkBytes = Base64.getDecoder().decode(chunk);
                baos.write(chunkBytes);
            }

            // Salvar o arquivo completo
            File outputFile = new File(this.folder, file.getFilename());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(baos.toByteArray());
            }

            System.out.println("Download do arquivo " + file.getFilename() + " finalizado.");

        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo final.");
            e.printStackTrace();
        }

    }

    private static void saveBase64ToFile(String base64Content, String fileName, File folder) {
        try {
            // Decodifica Base64 para bytes
            byte[] fileBytes = Base64.getDecoder().decode(base64Content);

            // Cria o caminho completo do arquivo no diretório de compartilhamento
            File outputFile = new File(folder, fileName);

            // Escreve o conteúdo no arquivo (sobrescreve se já existir)
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(fileBytes);
            fos.close();

        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendBye() {

        for (NeighborPeer p : this.neighbors) {

            if (p.getStatus().equals("ONLINE")) {

                ByeMessage message = new ByeMessage(this);
                message.send(p);
            }

        }

        this.isOnline = false;

        try {
            this.server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Saindo...");
    }

    public void increaseClock() {
        this.clock += 1;
        System.out.println("[" + Thread.currentThread().getName() + "]"
                + String.format("=> Atualizando relogio para %d", this.clock));
    }

    public NeighborPeer findNeighbor(String address, int port) {
        for (NeighborPeer p : this.neighbors) {
            if (p.getAddress().equals(address) && p.getDoor() == port) {
                return p;
            }
        }

        return null;
    }

    // Adiciona um novo vizinho se ele não existe na lista de vizinhos conhecidos.
    // Se já existe, o status dele é atualizado
    public void updateNeighbor(NeighborPeer p) {

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
            if (neighbor.getClock() < p.getClock()) {
                neighbor.setClock(p.getClock());
            }
        } else {
            this.neighbors.add(p);
            System.out.println("[" + Thread.currentThread().getName() + "]"
                    + String.format("Adicionando novo peer %s:%d status %s %d", p.getAddress(), p.getDoor(),
                            p.getStatus(), p.getClock()));
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

    public void setClock(int clock) {
        this.clock = clock;
    }

    public int getPort() {
        return this.door;
    }

    public String getAddress() {
        return this.address;
    }

    public File getSharedFolder() {
        return folder;
    }

    public void setSharedFolder(File folder) {
        this.folder = folder;
    }

    public int getChunckSize() {
        return chunckSize;
    }

    public void setChunckSize(int chunckSize) {
        this.chunckSize = chunckSize;
    }

    // Função da thread a ser executadas emoutra porta para simular peer
    @Override
    public void run() {
        try {
            while (this.isOnline) {

                // aguardando conexão na porta do servidor
                Socket client = this.server.accept();

                Thread handlerThread = new Thread( () -> {
                    
                })

                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

                Message received = null;

                try {
                    received = (Message) ois.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (received != null) {

                    // ao receber uma mensagem, atualiza-se o clock
                    this.clock = Math.max(this.clock, received.getClock());
                    this.increaseClock();

                    // envia a mensagem para o handler responsável
                    Message response = dispatcher.dispatch(this, received);

                    // envia a resposta do handler para o remetente
                    if (response != null) {
                        if (response.getType() != Message.Type.ACK) {
                            // ao enviar uma mensagem que não seja ACK para o remetente, aumenta-se o Clock
                            this.increaseClock();
                            response.setClock(this.getClock());

                            System.out.printf("%s Encaminhando resposta: %s\n",
                                    "[" + Thread.currentThread().getName() + "]",
                                    response.toString());

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
        System.out.println("Thread encerrada!");
    }

}
