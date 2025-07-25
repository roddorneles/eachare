import java.util.Objects;

public class NeighborPeer {
    private String address;
    private int door;
    private String status;
    private int clock = 0;

    public NeighborPeer(String address, int door, String status, int clock) {
        this.address = address;
        this.door = door;
        this.status = status;
        this.clock = clock;
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
        return String.format("%s:%d:%s:%d", address, door, status, clock);
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

    public void setClock(int clock) {
        this.clock = clock;
    }

    public int getClock() {
        return this.clock;
    }

}
