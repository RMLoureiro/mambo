package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class Disconnect extends ProtoMessage {
    static Host sender, del = null;
    static int hash;
    public static final short MSG_CODE = 111;

    public Disconnect(Host sender, int hash, Host del) {
        super(MSG_CODE);
        this.sender = sender;
        this.hash = hash;
        this.del = del;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            out.writeInt(hash);
            Host.serializer.serialize(sender, out);
        }

        @Override
        public Disconnect deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int code = in.readInt();
            return new Disconnect(host, code, null);
        }
    };

    public Host getSender(){ return sender;}

    public Host getDel(){ return del;}

    public int getHash(){return hash;}
    @Override
    public String toString() {
        return "Disconnect Message";
    }
}