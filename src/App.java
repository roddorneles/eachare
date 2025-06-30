import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {

    // cria uma lista de peers que são os vizinhos
    public static List<Peer> peers = new ArrayList<Peer>();
    public static Peer localPeer;
    public static File folder;
    public static File[] sharedFiles;

    private static final boolean DEBUG = false;

    public static void main(String[] args) throws Exception {

        String endpoint = "";
        String neighboursFile = "";
        String sharedDir = "";

        if (args == null || args.length == 0) {
            endpoint = "127.0.0.1:8005";
            neighboursFile = "vizinhos8005.txt";
            sharedDir = "compartilhar";
        } else {
            endpoint = args[0];
            neighboursFile = args[1];
            sharedDir = args[2];
        }

        // abre um socket na porta especificada no argumento
        String localPeerAddress = endpoint.split(":")[0];
        String localPeerDoor = endpoint.split(":")[1];

        // criando o peer "local"
        localPeer = new Peer(localPeerAddress, localPeerDoor);
        localPeer.setClock(0);

        try {
            BufferedReader br = new BufferedReader(new FileReader(neighboursFile));

            String nextLine = br.readLine();

            while (nextLine != null) {
                String peerAddress = nextLine.split(":")[0];
                String peerDoor = nextLine.split(":")[1];

                if (!DEBUG) {
                    localPeer.updateNeighbor(new NeighborPeer(peerAddress,
                            Integer.parseInt(peerDoor), "OFFLINE", 0));
                }

                if (DEBUG) {
                    peers.add(new Peer(peerAddress, peerDoor));
                }

                nextLine = br.readLine();
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }

        if (DEBUG) {
            for (Peer p : peers) {
                p.setName(p.getPeerName());
                p.start();
            }

            localPeer.updateNeighbor(new NeighborPeer("127.0.0.1", 8006, "ONLINE", 10));
            localPeer.updateNeighbor(new NeighborPeer("127.0.0.1", 8007, "ONLINE", 15));

            peers.get(0).updateNeighbor(new NeighborPeer("127.0.0.1", 8005, "ONLINE",
                    11));
            peers.get(0).updateNeighbor(new NeighborPeer("127.0.0.1", 8008, "ONLINE",
                    16));
            peers.get(0).updateNeighbor(new NeighborPeer("127.0.0.1", 8009, "ONLINE",
                    13));

            peers.get(1).updateNeighbor(new NeighborPeer("127.0.0.1", 8005, "ONLINE",
                    11));
            peers.get(1).updateNeighbor(new NeighborPeer("127.0.0.1", 8008, "ONLINE",
                    17));
            peers.get(1).updateNeighbor(new NeighborPeer("127.0.0.1", 8009, "ONLINE",
                    12));
        }

        // abre a pasta a ser compartilhada
        folder = new File(sharedDir);
        sharedFiles = folder.listFiles();

        localPeer.setSharedFolder(folder);

        localPeer.start();

        // inicializa o menu
        menu();

    }

    public static void menu() {

        Scanner sc = new Scanner(System.in);
        String userInput = "";

        do {

            System.out.println("Escolha um comando:");
            System.out.println("\t[1] Listar peers");
            System.out.println("\t[2] Obter peers");
            System.out.println("\t[3] Listar arquivos locais");
            System.out.println("\t[4] Buscar arquivos");
            System.out.println("\t[5] Exibir estatisticas");
            System.out.println("\t[6] Alterar tamanho de chunck");
            System.out.println("\t[9] Sair");

            userInput = sc.nextLine();

            switch (userInput) {
                case "1":
                    listPeers();
                    break;
                case "2":
                    getPeers();
                    break;
                case "3":
                    listLocalFiles();
                    break;
                case "4":
                    searchFile();
                    break;
                case "5":
                    displayStatistics();
                    break;
                case "6":
                    changeChunckSize();
                    break;
                case "9":
                    userInput = "";
                    leave();
                    break;
            }

        } while (!userInput.isEmpty());

    }

    public static void listPeers() {

        Scanner sc = new Scanner(System.in);

        System.out.println("Lista de peers:");
        System.out.println("\t[0] voltar para o menu anterior");
        int i = 1;

        List<NeighborPeer> neighbors = localPeer.getNeighbors();

        for (NeighborPeer p : neighbors) {
            System.out.println(
                    String.format("\t[%d] %s:%s %s %d", i, p.getAddress(), p.getDoor(), p.getStatus(), p.getClock()));
            i += 1;
        }

        String userInput = sc.nextLine();

        if (!userInput.equals("0")) {
            NeighborPeer selectedPeer = neighbors.get(Integer.parseInt(userInput) - 1);
            localPeer.sendHello(selectedPeer);
        }

    }

    public static void getPeers() {
        localPeer.sendGetPeers();
    }

    public static void listLocalFiles() {
        for (String s : localPeer.getSharedFolder().list()) {
            System.out.println(s);
        }
    }

    public static void searchFile() {

        Map<FoundFile, FoundFile> foundFilesMap = localPeer.sendList();
        List<FoundFile> foundFiles = new ArrayList<>(foundFilesMap.values());

        System.out.println("Arquivos encontrados na rede:");

        System.out.printf("  %-20s | %-8s | %-15s\n", "Nome", "Tamanho", "Peer");

        // Linha 0 - Cancelar
        System.out.printf("  [ %-2s] %-14s | %-8s | %-15s\n", "0", "<Cancelar>", "", "");

        for (int i = 0; i < foundFiles.size(); i++) {
            FoundFile foundFile = foundFiles.get(i);
            System.out.printf("  [ %-2d] %-14s | %-8s | %-15s\n", i + 1, foundFile.getFilename(), foundFile.getSize(),
                    foundFile.peersToStr());
        }

        Scanner sc = new Scanner(System.in);
        String userInput = sc.nextLine();

        if (!userInput.equals("0")) {

            FoundFile selectedFile = foundFiles.get(Integer.parseInt(userInput) - 1);
            localPeer.sendDl(selectedFile);
        }

    }

    public static void displayStatistics (){
        ConcurrentStatisticsCollector statistics = localPeer.getStatisticsCollector();
        statistics.displayStatistics();
    }

    public static void changeChunckSize() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Digite o novo tamanho de chunck: ");
        String userInput = sc.nextLine();

        int newChunkSize = Integer.parseInt(userInput);

        localPeer.setChunckSize(newChunkSize);

        System.out.println("\t Tamanho de chunck alterado: " + localPeer.getChunckSize());
    }

    public static void leave() {
        localPeer.sendBye();
    }
}
