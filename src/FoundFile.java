public class FoundFile {

    private String filename;
    private int size;
    private NeighborPeer peer;
    private String address;
    private int port;

    public FoundFile(String filename, int size, String address, int port) {
        this.filename = filename;
        this.size = size;
        this.address = address;
        this.port = port;
    }

    public String getFilename() {
        return filename;
    }

    public int getSize() {
        return size;
    }

    public NeighborPeer getPeer() {
        return peer;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

}
