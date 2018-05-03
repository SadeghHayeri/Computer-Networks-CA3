import java.io.IOException;
import java.util.concurrent.TimeUnit;

class PacketSender implements Runnable {

    private MyTCPSocket socket;
    private MyTCPPacket lastPacket;
    private boolean isPacketSet = false;

    public PacketSender(MyTCPSocket socket) {
        super();
        this.socket = socket;
    }

    public void setPacket(MyTCPPacket packet) {
        this.lastPacket = packet;
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
                            socket.datagramSendPacket(lastPacket);
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