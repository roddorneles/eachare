import java.util.Objects;

public class NeighborPeer {
    private String address;
    private int door;
    private String status;

    public NeighborPeer(String address, int door) {
        this.address = address;
        this.door = door;
        this.status = "OFFLINE";
    }

    public NeighborPeer(String address, int door, String status) {
        this.address = address;
        this.door = door;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NeighborPeer that = (NeighborPeer) o;

        return this.door == that.door && this.address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, door);
    }

    public void turnOn() {
        this.status = "ONLINE";
        System.out.println("[" + Thread.currentThread().getName() + "]"
                + String.format("Atualizando peer %s:%d status %s", this.getAddress(), this.getDoor(),
                        this.getStatus()));
    }

    public void turnOff() {
        this.status = "OFFLINE";
        System.out.println("[" + Thread.currentThread().getName() + "]"
                + String.format("Atualizando peer %s:%d status %s", this.getAddress(), this.getDoor(),
                        this.getStatus()));
    }

    public String getPeerName() {
        return String.format("%s:%d", address, door);
    }

    public String fullInfo() {
        return String.format("%s:%d:%s:0", address, door, status);
    }

    public String getAddress() {
        return address;
    }

    public int getDoor() {
        return door;
    }

    public String getStatus() {
        return this.status;
    }

}
