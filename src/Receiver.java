import java.util.Arrays;

public class Receiver {
    public static void main(String[] args) throws Exception {
        TCPServerSocket tcpServerSocket = new MyTCPServerSocket(Config.SERVER_PORT);
        TCPSocket tcpSocket = tcpServerSocket.accept();
        tcpSocket.receive("B");
        tcpSocket.close();
        tcpServerSocket.close();
    }
}
