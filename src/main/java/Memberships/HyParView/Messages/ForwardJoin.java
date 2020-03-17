package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class ForwardJoin extends ProtoMessage {
    public static final short MSG_CODE = 102;
    static Host sender, newNode;
    static int TTL;

    public ForwardJoin(Host sender, Host newNode, int TTL) {
        super(MSG_CODE);
        this.TTL = TTL;
        this.sender = sender;
        this.newNode = newNode;
    }
    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Host.serializer.serialize(sender, out);
            Host.serializer.serialize(newNode, out);
            out.writeInt(TTL);
        }

        @Override
        public ForwardJoin deserialize(ByteBuf in) throws IOException {
            Host sender = Host.serializer.deserialize(in);
            Host newNode = Host.serializer.deserialize(in);
            int TTL = in.readInt();
            return new ForwardJoin(sender, newNode, TTL);
        }
    };

    public Host getSender(){ return sender;}
    public Host getNewNode(){return newNode;}
    public int getTTL(){ return TTL;}


    @Override
    public String toString() {
        return "Forward Join Message";
    }
}
