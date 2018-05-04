import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Sender {
    public static void main(String[] args) throws Exception {
        MyTCPSocket tcpSocket = new MyTCPSocket(Config.SERVER_IP, Config.SERVER_PORT);
        tcpSocket.send("A");
        tcpSocket.close();
        tcpSocket.saveCongestionWindowPlot();
    }
}
