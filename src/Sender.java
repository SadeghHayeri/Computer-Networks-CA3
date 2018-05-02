public class Sender {

    public static void main(String[] args) throws Exception {
        MyTCPSocket tcpSocket = new MyTCPSocket(Config.SERVER_IP, Config.SERVER_PORT);
        tcpSocket.connect(Config.SERVER_IP, Config.SERVER_PORT);

        MyTCPPacket packet = new MyTCPPacket();

        byte[] data = new byte[20];
        packet.setData(data);
        tcpSocket.datagramSend(packet);

//        tcpSocket.send("sending.mp3");
////        tcpSocket.close();
////        tcpSocket.saveCongestionWindowPlot();
    }
}
