import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {

    public enum Type {
        HELLO,
        GET_PEERS,
        PEER_LIST,
        BYE,
        ACK
    }

    private String senderIp;
    private int senderPort;
    private int clock;
    private Type type;
    private List<String> args;

    public Message(Type type, String senderIp, int senderPort, int clock) {
        this.type = type;
        this.senderIp = senderIp;
        this.senderPort = senderPort;
        this.clock = clock;
        this.args = new ArrayList<String>();
    }

    @Override
    public String toString() {
        if (args.isEmpty()) {
            return String.format("\"%s:%s %d %s\"", this.senderIp, this.senderPort, this.clock, this.type);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }
            return String.format("\"%s:%s %d %s %s\"", this.senderIp, this.senderPort, this.clock, this.type,
                    sb.toString());
        }
    }

    public Type getType() {
        return type;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public int getSenderPort() {
        return senderPort;
    }

    public List<String> getArgs() {
        return args;
    }

    public void addArg(String arg) {
        this.args.add(arg);
    }

    public int getClock() {
        return this.clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

}
