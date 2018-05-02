import java.util.Arrays;

public class Receiver {
    public static void main(String[] args) throws Exception {

        MyTCPSocket tcpSocket = new MyTCPSocket(Config.SERVER_PORT);
        tcpSocket.accept();
//        tcpSocket.receive("receiving.mp3");
//        tcpSocket.close();
//        tcpServerSocket.close();
    }
}
