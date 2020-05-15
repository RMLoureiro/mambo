package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class Join extends ProtoMessage {
    private final Host sender;
    private final int hash;
    public static final short MSG_CODE = 101;

    public Join(Host sender, int hash) {
        super(MSG_CODE);
        this.sender = sender;
        this.hash = hash;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Join msg = (Join) message;
            Host.serializer.serialize(msg.sender, out);
            out.writeInt(msg.hash);
        }

        @Override
        public Join deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int code = in.readInt();
            return new Join(host, code);
        }
    };

    public Host getSender(){ return sender;}

    public int getHash(){ return hash;}

    @Override
    public String toString() {
        return "Join Message";
    }
}
