package Gossip.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class Leave extends ProtoMessage {
    private final int hostId;
    public static final short MSG_CODE = 110;

    public Leave(int id) {
        super(MSG_CODE);
        this.hostId = id;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Leave msg = (Leave) message;
            out.writeInt(msg.hostId);
        }

        @Override
        public Leave deserialize(ByteBuf in) throws IOException {
            int id =  in.readInt();
            return new Leave(id);
        }
    };

    public int getHostId(){ return hostId;}

    @Override
    public String toString() {
        return "Kill Pill Message";
    }
}
