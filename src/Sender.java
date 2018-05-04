import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Sender {

    public static void main(String[] args) throws Exception {
        MyTCPSocket tcpSocket = new MyTCPSocket(Config.SERVER_IP, Config.SERVER_PORT);
        tcpSocket.connect(Config.SERVER_IP, Config.SERVER_PORT);

        System.out.println("start!");
        tcpSocket.send("A");
        System.out.println("end!");

        tcpSocket.saveCongestionWindowPlot();

        while (true);
    }
}
