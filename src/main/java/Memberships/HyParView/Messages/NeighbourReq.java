package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class NeighbourReq extends ProtoMessage {
    private final Host sender;
    private final int priority;
    public static final short MSG_CODE = 104;

    public NeighbourReq(Host sender, int priority) {
        super(MSG_CODE);
        this.sender = sender;
        this.priority = priority;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            NeighbourReq msg = (NeighbourReq) message;
            Host.serializer.serialize(msg.sender, out);
            out.writeInt(msg.priority);
        }

        @Override
        public NeighbourReq deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int prio =  in.readInt();
            return new NeighbourReq(host, prio);
        }
    };

    public Host getSender(){ return sender;}

    public int getPriority(){ return priority;}

    @Override
    public String toString() {
        return "Neighbour request message with " + priority + "priority";
    }
}
