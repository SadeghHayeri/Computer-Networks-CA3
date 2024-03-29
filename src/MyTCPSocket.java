import sun.awt.Mutex;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MyTCPSocket extends TCPSocket {

    private EnhancedDatagramSocket socket;

    private InetAddress destinationIp;
    private int destinationPort;

    private ReceiveHandler receiveHandler;
    private Thread receiveHandlerThread;

    private SenderHandler senderHandler;
    private Thread senderHandlerThread;

    private long sourceSequenceNumber;
    private long destinationSequenceNumber;

    private long threshold = Config.STARTING_THRESHOLD;
    private long duplicateACK = 0;

    private Queue<MyTCPPacket> receivedQueue = new LinkedList<>();
    private Mutex receivedQueueMutex = new Mutex();
    private Queue<MyTCPPacket> sendQueue = new LinkedList<>();
    private Mutex sendQueueMutex = new Mutex();
    private Queue<MyTCPPacket> windowQueue = new LinkedList<>();
    private Mutex windowQueueMutex = new Mutex();

    public enum State {
        CLOSED, LISTEN, SYN_RECEIVED, SYN_SEND, ESTABLISHED;
    }
    private State state = State.CLOSED;

    public MyTCPSocket(String ip, int port) throws Exception {
        this.destinationIp = InetAddress.getByName(ip);
        this.destinationPort = port;
        this.socket = new EnhancedDatagramSocket(Config.CLIENT_PORT);
        this.socket.connect(destinationIp, port);

        this.senderHandler = new SenderHandler(this);
        this.senderHandlerThread = new Thread(senderHandler);
        this.senderHandlerThread.start();

        connect();
    }

    public MyTCPSocket(int port) throws SocketException {
        this.socket = new EnhancedDatagramSocket(port);

//        this.senderHandler = new SenderHandler(this);
//        this.senderHandlerThread = new Thread(senderHandler);
//        this.senderHandlerThread.start();
    }

    public MyTCPSocket accept() throws Exception {
        state = State.LISTEN;

        //TODO: do it better! (use received packed address)
        destinationIp = InetAddress.getByName(Config.CLIENT_IP);
        destinationPort = Config.CLIENT_PORT;

        // start receiver thread!
        receiveHandler = new ReceiveHandler(this);
        receiveHandlerThread = new Thread(receiveHandler);
        receiveHandlerThread.start();

        // stay for connection
        while (true) {
            TimeUnit.MILLISECONDS.sleep(Config.TIMEOUT);
            if (state == State.ESTABLISHED)
                return this;
        }
    }

    private void connect() throws Exception {
        state = State.SYN_SEND;

        sourceSequenceNumber = 0;

        // send SYN packet
        MyTCPPacket synPacket = new MyTCPPacket();
        synPacket.SYN = true;
        synPacket.setSequenceNumber(sourceSequenceNumber);
        synPacket.setData(Config.PHANTON_BYTE);

        // start receiver thread!
        receiveHandler = new ReceiveHandler(this, synPacket);
        receiveHandlerThread = new Thread(receiveHandler);
        receiveHandlerThread.start();

        // stay for connection
        while (true) {
            TimeUnit.MILLISECONDS.sleep(Config.TIMEOUT);
            if(state == State.ESTABLISHED) {
                return;
            }
        }
    }

    public void datagramSend(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(
                data, data.length, destinationIp, destinationPort);
        socket.send(packet);
    }

    public void datagramSendPacket(MyTCPPacket packet) throws IOException {
        datagramSend(packet.toBytes());
    }

    public byte[] datagramReceive() throws IOException {
        byte[] buf = new byte[Config.DEFAULT_PAYLOAD_LIMIT_IN_BYTES];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);

        socket.receive(dp);
        return Arrays.copyOfRange(dp.getData(), 0, dp.getLength());
    }

    public MyTCPPacket datagramReceivePacket() throws Exception {
        return new MyTCPPacket(datagramReceive());
    }

    public void send(MyTCPPacket packet) {
        assert state == State.ESTABLISHED;
        sendQueueMutex.lock();
        sendQueue.add(packet);
        sendQueueMutex.unlock();
    }

    public MyTCPPacket receive() throws InterruptedException {
        assert state == State.ESTABLISHED;
        receivedQueueMutex.lock();
        while (receivedQueue.isEmpty()) {
            receivedQueueMutex.unlock();
            TimeUnit.MILLISECONDS.sleep(Config.TIMEOUT);
            receivedQueueMutex.lock();
        }
        MyTCPPacket packet = receivedQueue.poll();
        receivedQueueMutex.unlock();
        return packet;
    }

    @Override
    public void send(String pathToFile) throws Exception {
        assert state == State.ESTABLISHED;
        Path path = Paths.get(pathToFile);
        byte[] data = Files.readAllBytes(path);

        while (data.length > 0) {
            int packetSize = Math.min(data.length, Config.MAX_SPLIT_SIZE);
            byte[] packetData = Arrays.copyOfRange(data, 0, packetSize);
            data = Arrays.copyOfRange(data, packetSize, data.length);

            MyTCPPacket packet = new MyTCPPacket();
            packet.setSequenceNumber(getSourceSequenceNumber(packetSize));
            packet.setData(packetData);
            send(packet);
        }

        MyTCPPacket packet = new MyTCPPacket();
        packet.setSequenceNumber(getSourceSequenceNumber(0));
        send(packet);
    }

    private long getSourceSequenceNumber(long dataSize) {
        long oldSeqNumber = sourceSequenceNumber;
        sourceSequenceNumber += dataSize;
        return oldSeqNumber;
    }

    @Override
    public void receive(String pathToFile) throws Exception {
        assert state == State.ESTABLISHED;
        FileOutputStream stream = new FileOutputStream(pathToFile);
        try {
            while (true) {
                MyTCPPacket packet = receive();

                if(packet.getData().length == 0)
                    break;

                stream.write(packet.getData());
            }
        } finally {
            stream.close();
        }
    }

    @Override
    public void close() throws Exception {
//        senderHandler.stop();
//        senderHandlerThread.interrupt();
//        receiveHandler.stop();
//        receiveHandlerThread.interrupt();
//        socket.close();
    }

    @Override
    public long getSSThreshold() {
        return threshold;
    }

    @Override
    public long getWindowSize() {
        return senderHandler.getWindowSize();
    }


    class SenderHandler implements Runnable {

        private MyTCPSocket socket;

        private int windowSize = 1;
        private long timer = 0;
        private long lastTime = System.currentTimeMillis();
        private boolean running = true;

        public int getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(int windowSize) {
            this.windowSize = windowSize;
        }

        public SenderHandler(MyTCPSocket socket) {
            this.socket = socket;
        }

        public void stop() {
            running = false;
        }

        public void sendAllWindow() {
            windowQueueMutex.lock();
            for (MyTCPPacket packet : windowQueue) {
                try {
                    socket.datagramSendPacket(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            windowQueueMutex.unlock();
        }

        @Override
        public void run() {
            while(running) {

                System.out.println(String.format("%d  %d(%d)", sendQueue.size(), windowSize, windowQueue.size()));

                long now = System.currentTimeMillis();
                timer += (now - lastTime);
                lastTime = now;

                if (timer > Config.TIMEOUT) {
                    timer = 0;
                    sendAllWindow();
                }

                // check window diff
                windowQueueMutex.lock();
                sendQueueMutex.lock();
                {
                    int windowDiff = windowSize - windowQueue.size();
                    for (int i = 0; i < windowDiff; i++) {
                        if (!sendQueue.isEmpty()) {
                            MyTCPPacket packet = sendQueue.poll();
                            try {
                                socket.datagramSendPacket(packet);
                                windowQueue.add(packet);
                                timer = 0;
                            } catch (IOException e) {
                                sendQueue.add(packet);
                                e.printStackTrace();
                            }
                        }
                    }
                }
                sendQueueMutex.unlock();
                windowQueueMutex.unlock();
            }
        }
    }

    class ReceiveHandler implements Runnable {

        private MyTCPSocket socket;
        private PacketSender packetSender;
        private Thread packetSenderThread;
        private boolean running = true;

        public ReceiveHandler(MyTCPSocket socket) {
            this.socket = socket;
            packetSender = new PacketSender(socket);
            packetSenderThread = new Thread(packetSender);
            packetSenderThread.start();
        }

        public ReceiveHandler(MyTCPSocket socket, MyTCPPacket packet) {
            this.socket = socket;
            packetSender = new PacketSender(socket);
            packetSenderThread = new Thread(packetSender);
            packetSenderThread.start();

            packetSender.setPacket(packet);
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    MyTCPPacket receivedPacket = datagramReceivePacket();
                    System.out.println(String.format("new packet - %s", state));
                    System.out.println(receivedPacket.toString());

                    if(receivedPacket.RST)
                        state = MyTCPSocket.State.LISTEN;

                    switch (state) {
                        case CLOSED:
                            break;

                        case LISTEN:
                            if(receivedPacket.SYN) {
                                state = MyTCPSocket.State.SYN_RECEIVED;
                                destinationSequenceNumber = receivedPacket.getSequenceNumber();

                                MyTCPPacket packet = new MyTCPPacket();
                                packet.SYN = true;
                                packet.setSequenceNumber(sourceSequenceNumber);
                                packet.ACK = true;
                                long serverACKNumber = destinationSequenceNumber + receivedPacket.getData().length;
                                packet.setAcknowledgmentNumber(serverACKNumber);
                                packet.setData(Config.PHANTON_BYTE);
                                packetSender.setPacket(packet);
                            }
                            break;

                        case SYN_RECEIVED:
                            if(receivedPacket.ACK) {
                                boolean isExceptedACKNumber = receivedPacket.getAcknowledgmentNumber() == sourceSequenceNumber + 1;
                                if (isExceptedACKNumber) {
                                    state = MyTCPSocket.State.ESTABLISHED;
                                }
                            }
                            break;

                        case SYN_SEND:
                            boolean isExceptedACKNumber = receivedPacket.getAcknowledgmentNumber() == sourceSequenceNumber + 1;
                            if(receivedPacket.SYN && receivedPacket.ACK && isExceptedACKNumber) {
                                destinationSequenceNumber = receivedPacket.getSequenceNumber();
                                state = MyTCPSocket.State.ESTABLISHED;
                            }
                            break;

                        case ESTABLISHED:
                            packetSender.stop();
                            packetSenderThread.interrupt();

                            // SYN-ACK
                            if(receivedPacket.SYN && receivedPacket.ACK) {
                                MyTCPPacket ACKPacket = new MyTCPPacket();
                                ACKPacket.ACK = true;
                                long ackNumber = destinationSequenceNumber + receivedPacket.getData().length;
                                ACKPacket.setAcknowledgmentNumber(ackNumber);
                                socket.datagramSendPacket(ACKPacket);
                            }

                            // ACK
                            else if(!receivedPacket.SYN && receivedPacket.ACK) {
                                long ACKNumber = receivedPacket.getAcknowledgmentNumber();
                                windowQueueMutex.lock();
                                while (!windowQueue.isEmpty()) {
                                    MyTCPPacket windowHeadPacket = windowQueue.peek();
                                    long expectedACKNumber = windowHeadPacket.getSequenceNumber() + windowHeadPacket.getData().length;
                                    if(ACKNumber >= expectedACKNumber) {
                                        windowQueue.poll();

                                        int windowSize = senderHandler.getWindowSize();
                                        if(windowSize < threshold)
                                            senderHandler.setWindowSize(windowSize + Config.MSS);
                                        else
                                            senderHandler.setWindowSize(windowSize + (int)(Math.pow(Config.MSS, 2) / windowSize));
                                        duplicateACK = 0;

                                    } else {
                                        duplicateACK++;
                                        if(duplicateACK >= Config.MAX_DUPLICATE_ACK) {
                                            threshold = senderHandler.getWindowSize() / 2;
                                            senderHandler.setWindowSize(1);
                                            duplicateACK = 0;
                                        }
                                        break;
                                    }
                                    onWindowChange();
                                }
                                windowQueueMutex.unlock();
                            }

                            // DATA
                            else {
                                System.out.println(String.format("%d - %d += %d", receivedPacket.getSequenceNumber(), destinationSequenceNumber, receivedPacket.getData().length));
                                if(receivedPacket.getSequenceNumber() == destinationSequenceNumber) {
                                    destinationSequenceNumber += receivedPacket.getData().length;
                                    receivedQueueMutex.lock();
                                    receivedQueue.add(receivedPacket);
                                    receivedQueueMutex.unlock();
                                }
                                MyTCPPacket ACKPacket = new MyTCPPacket();
                                ACKPacket.ACK = true;
                                ACKPacket.setAcknowledgmentNumber(destinationSequenceNumber);
                                socket.datagramSendPacket(ACKPacket);
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
