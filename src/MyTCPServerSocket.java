
public class MyTCPServerSocket extends TCPServerSocket {
    public MyTCPServerSocket(int port) throws Exception {
        super(port);
    }

    @Override
    public TCPSocket accept() throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }
}
