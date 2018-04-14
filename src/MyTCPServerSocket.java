import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MyTCPServerSocket extends TCPServerSocket {

    public enum State {
        CLOSED, LISTEN, SYN_RCVD, SYN_SEND, ESTABLISHED;
    }

    private DatagramSocket socket;
    private State state;

    public MyTCPServerSocket(int port) throws Exception {
        super(port);
        this.socket = new EnhancedDatagramSocket(Config.PORT);
        this.state = State.CLOSED;
    }

    @Override
    public TCPSocket accept() throws Exception {

        MyTCPSocket socket = new MyTCPSocket(Config.IP, Config.PORT);

        MyTCPPacket SYNMessage = new MyTCPPacket();

        SYNMessage.SYN = true;

        long seqNumber = Utils.randomLong(Math.pow(2, 32));
        SYNMessage.setSequenceNumber(seqNumber);

        socket.send(SYNMessage);
//        System.out.println("recived!");
//        byte[] r = packet.getData();
//        System.out.println(new String(r));
        return socket;
    }

    @Override
    public void close() throws Exception {
//        throw new RuntimeException("Not implemented!");
    }
}
