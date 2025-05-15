import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    // cria uma lista de peers que são os vizinhos
    public static List<Peer> peers = new ArrayList<Peer>();
    public static Peer localPeer;

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");

        String endpoint = "";
        String neighboursFile = "";
        String sharedDir = "";

        if (args == null || args.length == 0) {
            endpoint = "127.0.0.1:8000";
            neighboursFile = "vizinhos.txt";
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

        try {
            BufferedReader br = new BufferedReader(new FileReader(neighboursFile));

            String nextLine = br.readLine();

            while (nextLine != null) {
                String peerAddress = nextLine.split(":")[0];
                String peerDoor = nextLine.split(":")[1];

                System.out.println("Adicionando novo peer " + nextLine + " status OFFLINE");
                localPeer.addNeighbor(new NeighborPeer(peerAddress, Integer.parseInt(peerDoor)));

                peers.add(new Peer(peerAddress, peerDoor));

                nextLine = br.readLine();
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }

        for (Peer p : peers) {
            p.start();
        }

        // abre a pasta a ser compartilhada
        File folder = new File(sharedDir);
        File[] sharedFiles = folder.listFiles();

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
                case "9":
                    userInput = "";
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
            System.out.println(String.format("\t[%d] %s:%s %s", i, p.getAddress(), p.getDoor(), p.getStatus()));
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
}
