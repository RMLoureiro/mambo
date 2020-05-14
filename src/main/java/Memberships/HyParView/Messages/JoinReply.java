package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class JoinReply extends ProtoMessage {
    static Host sender, newNode = null;
    static int hash;
    public static final short MSG_CODE = 103;

    public JoinReply(Host sender, int hash, Host newNode) {
        super(MSG_CODE);
        this.sender = sender;
        this.hash = hash;
        this.newNode = newNode;
    }

    public JoinReply(Host sender, int hash) {
        super(MSG_CODE);
        this.sender = sender;
        this.hash = hash;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Host.serializer.serialize(sender, out);
            out.writeInt(hash);
        }

        @Override
        public JoinReply deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int code = in.readInt();
            return new JoinReply(host, code);
        }
    };

    public Host getSender(){ return sender;}

    public Host getNewNode(){ return  newNode;}

    public int getHash(){ return hash;}

    @Override
    public String toString() {
        return "Join reply Message";
    }
}
