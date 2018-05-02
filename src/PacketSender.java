import java.io.IOException;
import java.util.concurrent.TimeUnit;

class PacketSender implements Runnable {

    private MyTCPSocket socket;
    private MyTCPPacket lastSentPacket;
    private boolean isPacketSet = false;

    public PacketSender(MyTCPSocket socket) {
        super();
        this.socket = socket;
    }

    public void setPacket(MyTCPPacket packet) {
        this.lastSentPacket = packet;
        this.isPacketSet = true;
    }

    public void stop() {
        isPacketSet = false;
    }

    @Override
    public void run() {
        while (true) {
            while (!Thread.interrupted()) {
                try {
                    if(isPacketSet) {
                        try {
                            socket.datagramSend(lastSentPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(Config.TIMEOUT);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}