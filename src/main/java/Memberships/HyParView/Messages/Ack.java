package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class Ack extends ProtoMessage {
    static Host sender;
    static int hash;
    public static final short MSG_CODE = 112;

    public Ack(int hash, Host sender) {
        super(MSG_CODE);
        this.hash = hash;
        this.sender = sender;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Host.serializer.serialize(sender, out);
            out.writeInt(hash);
        }

        @Override
        public Ack deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int code = in.readInt();
            return new Ack(code, host);
        }
    };

    public int getHash(){ return hash;}

    public Host getSender(){return sender;}
    @Override
    public String toString() {
        return "Ack Message";
    }
}
