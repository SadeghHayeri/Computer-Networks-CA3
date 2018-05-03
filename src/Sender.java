import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Sender {

    public static void main(String[] args) throws Exception {
        MyTCPSocket tcpSocket = new MyTCPSocket(Config.SERVER_IP, Config.SERVER_PORT);

        System.out.println("go to connect!");
        tcpSocket.connect(Config.SERVER_IP, Config.SERVER_PORT);
        System.out.println("connected!");

        System.out.println("start!");
        tcpSocket.send("A.pdf");
        System.out.println("end!");

        while (true);
    }
}
