package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class NeighbourReq extends ProtoMessage {
    static Host sender;
    static int priority;
    public static final short MSG_CODE = 104;

    public NeighbourReq(Host sender, int priority) {
        super(MSG_CODE);
        this.sender = sender;
        this.priority = priority;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Host.serializer.serialize(sender, out);
            out.writeInt(priority);
        }

        @Override
        public NeighbourReq deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int priority =  in.readInt();
            return new NeighbourReq(host, priority);
        }
    };

    public Host getSender(){ return sender;}

    public int getPriority(){ return priority;}

    @Override
    public String toString() {
        return "Neighbour request message with " + priority + "priority";
    }
}
