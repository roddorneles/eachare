import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FoundFile {

    private String filename;
    private int size;
    private List<NeighborPeer> peers;
    private String address;
    private int port;

    public FoundFile(String filename, int size, String address, int port) {
        this.filename = filename;
        this.size = size;
        this.address = address;
        this.port = port;
        this.peers = new ArrayList<NeighborPeer>();
    }

    public FoundFile(String filename, int size) {
        this.filename = filename;
        this.size = size;
        this.peers = new ArrayList<NeighborPeer>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FoundFile that = (FoundFile) o;

        return this.filename.equals(that.filename) && this.size == (that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, size);
    }

    public String getFilename() {
        return filename;
    }

    public int getSize() {
        return size;
    }

    public void addPeer(NeighborPeer neighborPeer) {
        if (!this.peers.contains(neighborPeer)) {
            this.peers.add(neighborPeer);
        }
    }

    public String peersToStr() {
        // Junta todos os peers em uma string separada por vírgula
        String peersStr = this.peers.stream()
                .map(peer -> peer.getAddress() + ":" + peer.getDoor())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return peersStr;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

}
