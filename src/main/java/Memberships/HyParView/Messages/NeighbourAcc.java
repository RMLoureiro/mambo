package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class NeighbourAcc extends ProtoMessage {
    static Host sender, newNode = null;
    static int hash;
    public static final short MSG_CODE = 106;

    public NeighbourAcc(Host sender, int hash, Host newNode) {
        super(MSG_CODE);
        this.sender = sender;
        this.hash = hash;
        this.newNode = newNode;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            out.writeInt(hash);
            Host.serializer.serialize(sender, out);
        }

        @Override
        public NeighbourAcc deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int code = in.readInt();
            return new NeighbourAcc(host, code, null);
        }
    };

    public Host getSender(){ return sender;}

    public Host getNewNode(){ return  newNode;}

    public int getHash(){ return hash;}

    @Override
    public String toString() {
        return "Neighbour accept Message";
    }
}
