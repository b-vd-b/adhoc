package client;

import datatype.AckMessage;
import datatype.Packet;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class PacketManager {

    private HashMap<InetAddress, List<Packet>> receivedPackets;
    private HashMap<InetAddress, List<Packet>> sentPackets;
    private HashMap<InetAddress, Long> sequenceNumbers;
    private Map<Packet, Integer> unacknowledgedPackets;

    PacketManager() {
        this.receivedPackets = new HashMap<>();
        this.sentPackets = new HashMap<>();
        this.sequenceNumbers = new HashMap<>();
        this.unacknowledgedPackets = new ConcurrentHashMap<>();
    }

    void addReceivedPacket(Packet packet) {
        receivedPackets.putIfAbsent(packet.getSourceAddress(), new ArrayList<>());
        receivedPackets.get(packet.getSourceAddress()).add(packet);
    }

    void addSentPacket(Packet packet) throws IOException {
        sentPackets.putIfAbsent(packet.getDestinationAddress(), new ArrayList<>());
        sentPackets.get(packet.getDestinationAddress()).add(packet);
        addUnacknowledgedPacket(packet);
        sequenceNumbers.put(packet.getDestinationAddress(), packet.getSequenceNumber() + packet.getLength());
    }

    private void addUnacknowledgedPacket(Packet packet) {
        unacknowledgedPackets.put(packet, 0);
    }

    Map<Packet, Integer> getUnacknowledgedPackets() {
        return unacknowledgedPackets;
    }

    void parseAcknowledgement(Packet ack) throws IOException {
        Packet acknowledgedPacket = null;

        for (Packet packet : unacknowledgedPackets.keySet()) {
            if (packet.getDestinationAddress().equals(ack.getSourceAddress())) {
                if (((AckMessage) ack.getPayload()).getAckNumber() == packet.getSequenceNumber() + packet.getLength()) {
                    acknowledgedPacket = packet;
                    break;
                }
            }
        }

        if (acknowledgedPacket != null) {
            unacknowledgedPackets.remove(acknowledgedPacket);
        }
    }

    boolean isKnownPacket(Packet packet) throws IOException {
        if (receivedPackets.get(packet.getSourceAddress()) == null) {
            return false;
        }

        for (Packet receivedPacket : receivedPackets.get(packet.getSourceAddress())) {
            if (receivedPacket.getDestinationAddress().equals(packet.getDestinationAddress())) {
                if (receivedPacket.getSequenceNumber() == packet.getSequenceNumber()) {
                    if (receivedPacket.getLength() == packet.getLength()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    long getSequenceNumber(InetAddress address) {
        Long sequence = sequenceNumbers.get(address);
        return (sequence == null) ? 0 : sequence;
    }

}
