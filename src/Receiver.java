import java.util.Arrays;

public class Receiver {
    public static void main(String[] args) throws Exception {

        MyTCPSocket tcpSocket = new MyTCPSocket(Config.SERVER_PORT);
        tcpSocket.accept();

        System.out.println("stay!");
        tcpSocket.receive("B");
        System.out.println("received!");

        while (true);
    }
}
