import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.util.Arrays;

public class MyTCPPacket {
    private static final int HEADER_SIZE = 20;

    private long sourcePort = 0;
    private long destinationPort = 0;
    private long sequenceNumber = 0;
    private long acknowledgmentNumber = 0;
    private long dataOffset = 0;

    public boolean NS = false;
    public boolean CWR = false;
    public boolean ECE = false;
    public boolean URG = false;
    public boolean ACK = false;
    public boolean PSH = false;
    public boolean RST = false;
    public boolean SYN = false;
    public boolean FIN = false;

    private long windowSize = 5;
    private long checksum = 0;
    private long urgentPointer = 0;

    private byte[] data = new byte[0];

    public MyTCPPacket() {}

    public MyTCPPacket(byte[] rawData) {
        assert rawData.length >= HEADER_SIZE;

        byte[] tmpLong = new byte[8];

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 0, tmpLong, 6, 2);
        sourcePort = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 2, tmpLong, 6, 2);
        destinationPort = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 4, tmpLong, 4, 4);
        sequenceNumber = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 8, tmpLong, 4, 4);
        acknowledgmentNumber = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 12, tmpLong, 7, 1);

        NS = (tmpLong[7] & 1) == 1;
        tmpLong[7] = (byte)(tmpLong[7] >> 4);
        dataOffset = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 13, tmpLong, 7, 1);
        CWR = (((1 << 7) & tmpLong[7]) != 0);
        ECE = (((1 << 6) & tmpLong[7]) != 0);
        URG = (((1 << 5) & tmpLong[7]) != 0);
        ACK = (((1 << 4) & tmpLong[7]) != 0);
        PSH = (((1 << 3) & tmpLong[7]) != 0);
        RST = (((1 << 2) & tmpLong[7]) != 0);
        SYN = (((1 << 1) & tmpLong[7]) != 0);
        FIN = (((1 << 0) & tmpLong[7]) != 0);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 14, tmpLong, 6, 2);
        windowSize = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 16, tmpLong, 6, 2);
        checksum = Utils.bytesToLong(tmpLong);

        Arrays.fill(tmpLong, (byte) 0X0);
        System.arraycopy(rawData, 18, tmpLong, 6, 2);
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
        System.arraycopy(tmpLong, 6, result, 0, 2);

        tmpLong = Utils.longToBytes(destinationPort);
        System.arraycopy(tmpLong, 6, result, 2, 2);

        tmpLong = Utils.longToBytes(sequenceNumber);
        System.arraycopy(tmpLong, 4, result, 4, 4);

        tmpLong = Utils.longToBytes(acknowledgmentNumber);
        System.arraycopy(tmpLong, 4, result, 8, 4);

        tmpLong = Utils.longToBytes(dataOffset);
        tmpLong[7] <<= 4;
        tmpLong[7] |= (NS ? 1 : 0);
        System.arraycopy(tmpLong, 7, result, 12, 1);

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
        System.arraycopy(tmpLong, 6, result, 14, 2);

        tmpLong = Utils.longToBytes(checksum);
        System.arraycopy(tmpLong, 6, result, 16, 2);

        tmpLong = Utils.longToBytes(urgentPointer);
        System.arraycopy(tmpLong, 6, result, 18, 2);

        System.arraycopy(data, 0, result, 20, data.length);

        return result;
    }

    //////////////////////// getter and setters ////////////////////////
    public long getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(long sourcePort) {
        assert sourcePort < Math.pow(2, 16);
        this.sourcePort = sourcePort;
    }

    public long getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(long destinationPort) {
        assert destinationPort < Math.pow(2, 16);
        this.destinationPort = destinationPort;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        assert sequenceNumber < Math.pow(2, 32);
        this.sequenceNumber = sequenceNumber;
    }

    public long getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public void setAcknowledgmentNumber(long acknowledgmentNumber) {
        assert acknowledgmentNumber < Math.pow(2, 32);
        this.acknowledgmentNumber = acknowledgmentNumber;
    }

    public long getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(long dataOffset) {
        assert dataOffset < Math.pow(2, 4);
        this.dataOffset = dataOffset;
    }

    public long getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(long windowSize) {
        assert windowSize < Math.pow(2, 16);
        this.windowSize = windowSize;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        assert checksum < Math.pow(2, 16);
        this.checksum = checksum;
    }

    public long getUrgentPointer() {
        return urgentPointer;
    }

    public void setUrgentPointer(long urgentPointer) {
        assert urgentPointer < Math.pow(2, 16);
        this.urgentPointer = urgentPointer;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String toString() {
        return String.format("SYN: %s - ACK: %s - DataLength: %d - SYN(%d) - ACK(%d)", SYN?"YES":"NO", ACK?"YES":"NO", data.length, sequenceNumber, acknowledgmentNumber);
    }
}
