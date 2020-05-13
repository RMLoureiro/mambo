package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class Ack extends ProtoMessage {
    static int hash;
    public static final short MSG_CODE = 112;

    public Ack(int hash) {
        super(MSG_CODE);
        this.hash = hash;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            out.writeInt(hash);
        }

        @Override
        public Ack deserialize(ByteBuf in) throws IOException {
            int hash = in.readInt();;
            return new Ack(hash);
        }
    };

    public int getHash(){ return hash;}

    @Override
    public String toString() {
        return "Ack Message";
    }
}
