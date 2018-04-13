import java.lang.reflect.Array;
import java.util.Arrays;

public class MyTCPPacket {
    private static final int HEADER_SIZE = 20;

    //DONE: user unsigned int -> if numbers go up it can be dangerous!
    public long sourcePort = 0;
    public long destinationPort = 0;
    public long sequenceNumber = 0;
    public long acknowledgmentNumber = 0;
    public long dataOffset = 0;

    public boolean NS = false;
    public boolean CWR = false;
    public boolean ECE = false;
    public boolean URG = false;
    public boolean ACK = false;
    public boolean PSH = false;
    public boolean RST = false;
    public boolean SYN = false;
    public boolean FIN = false;

    public long windowSize = 0;
    public long checksum = 0;
    public long urgentPointer = 0;

    public byte[] data = new byte[0];

    public MyTCPPacket() {}

    public MyTCPPacket(byte[] rawData) {
        assert rawData.length >= HEADER_SIZE;

        byte[] tmpLong = new byte[8];

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 0, tmpLong, 0, 2);
        sourcePort = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 2, tmpLong, 0, 2);
        destinationPort = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 4, tmpLong, 0, 4);
        sequenceNumber = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 8, tmpLong, 0, 4);
        acknowledgmentNumber = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 12, tmpLong, 0, 1);
        tmpLong[0] = (byte)(tmpLong[0] >> 4);
        dataOffset = Utils.bytesToLong(tmpLong);
        NS = (tmpLong[0] & 1) == 1;

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 13, tmpLong, 0, 1);
        CWR = (((1 << 7) & tmpLong[0]) != 0);
        ECE = (((1 << 6) & tmpLong[0]) != 0);
        URG = (((1 << 5) & tmpLong[0]) != 0);
        ACK = (((1 << 4) & tmpLong[0]) != 0);
        PSH = (((1 << 3) & tmpLong[0]) != 0);
        RST = (((1 << 2) & tmpLong[0]) != 0);
        SYN = (((1 << 1) & tmpLong[0]) != 0);
        FIN = (((1 << 0) & tmpLong[0]) != 0);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 14, tmpLong, 0, 2);
        windowSize = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 16, tmpLong, 0, 2);
        checksum = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 18, tmpLong, 0, 2);
        urgentPointer = Utils.bytesToLong(tmpLong);

        final int dataSize = rawData.length - HEADER_SIZE;
        data = new byte[dataSize];
        System.arraycopy(rawData, 20, data, 0, dataSize);
    }

    public byte[] toBytes() {
        byte[] result = new byte[HEADER_SIZE + data.length];
        Arrays.fill(result, (byte) 0X0);

        byte[] tmpLong;

        tmpLong = Utils.longToBytes(sourcePort);
        System.arraycopy(tmpLong, 0, result, 0, 2);

        tmpLong = Utils.longToBytes(destinationPort);
        System.arraycopy(tmpLong, 0, result, 2, 2);

        tmpLong = Utils.longToBytes(sequenceNumber);
        System.arraycopy(tmpLong, 0, result, 4, 4);

        tmpLong = Utils.longToBytes(acknowledgmentNumber);
        System.arraycopy(tmpLong, 0, result, 8, 4);

        tmpLong = Utils.longToBytes(dataOffset);
        tmpLong[0] <<= 4;
        tmpLong[0] |= (NS ? 1 : 0);
        System.arraycopy(tmpLong, 0, result, 12, 1);

        Arrays.fill(tmpLong, (byte) 0X0);
        tmpLong[0] |= ((CWR?1:0) << 7) |
                        ((ECE?1:0) << 6) |
                        ((URG?1:0) << 5) |
                        ((ACK?1:0) << 4) |
                        ((PSH?1:0) << 3) |
                        ((RST?1:0) << 2) |
                        ((SYN?1:0) << 1) |
                        ((FIN?1:0) << 0);
        System.arraycopy(tmpLong, 0, result, 13, 1);

        tmpLong = Utils.longToBytes(windowSize);
        System.arraycopy(tmpLong, 0, result, 14, 2);

        tmpLong = Utils.longToBytes(checksum);
        System.arraycopy(tmpLong, 0, result, 16, 2);

        tmpLong = Utils.longToBytes(urgentPointer);
        System.arraycopy(tmpLong, 0, result, 18, 2);

        System.arraycopy(data, 0, result, 20, data.length);

        return result;
    }
}
