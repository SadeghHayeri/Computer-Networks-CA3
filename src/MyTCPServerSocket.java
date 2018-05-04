public class MyTCPServerSocket extends TCPServerSocket {
    private int port;
    private MyTCPSocket socket;
    public MyTCPServerSocket(int port) throws Exception {
        super(port);
        this.port = port;
    }

    @Override
    public TCPSocket accept() throws Exception {
        this.socket = new MyTCPSocket(port);
        socket.accept();
        return socket;
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }
}