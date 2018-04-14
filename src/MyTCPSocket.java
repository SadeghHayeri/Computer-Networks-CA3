import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class MyTCPSocket extends TCPSocket {

    private DatagramSocket datagramSocket;
    private InetAddress receiverAddress;
    private int port;

    public MyTCPSocket(String ip, int port) throws Exception {
        super(ip, port);
        this.datagramSocket = new DatagramSocket();
        this.receiverAddress = InetAddress.getByName(ip);
        this.port = port;
    }

    public void datagramSend(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(
                data, data.length, this.receiverAddress, this.port);
        this.datagramSocket.send(packet);
    }

    public void send(MyTCPPacket packet) throws IOException {
        datagramSend(packet.toBytes());
    }

    @Override
    public void send(String pathToFile) throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void receive(String pathToFile) throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getSSThreshold() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getWindowSize() {
        throw new RuntimeException("Not implemented!");
    }
}
