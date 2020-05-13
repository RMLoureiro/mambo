package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class Disconnect extends ProtoMessage {
    static Host sender;
    public static final short MSG_CODE = 111;

    public Disconnect(Host sender) {
        super(MSG_CODE);
        this.sender = sender;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Host.serializer.serialize(sender, out);
        }

        @Override
        public Disconnect deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            return new Disconnect(host);
        }
    };

    public Host getSender(){ return sender;}

    @Override
    public String toString() {
        return "Disconnect Message";
    }
}
