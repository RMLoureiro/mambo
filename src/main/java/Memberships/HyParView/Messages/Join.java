package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class Join extends ProtoMessage {
    static Host sender;
    public static final short MSG_CODE = 101;

    public Join(Host sender) {
        super(MSG_CODE);
        this.sender = sender;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Host.serializer.serialize(sender, out);
        }

        @Override
        public Join deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            return new Join(host);
        }
    };

    public Host getSender(){ return sender;}

    @Override
    public String toString() {
        return "Join Message";
    }
}
