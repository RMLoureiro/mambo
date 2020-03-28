package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class ShuffleReply extends ProtoMessage {

    static Host sender;
    public static final short MSG_CODE = 109;
    static HashSet<Host> k, kRply;

    public ShuffleReply(Host sender, HashSet<Host> k, HashSet<Host> kRply) {
        super(MSG_CODE);
        this.sender = sender;
        this.k = k;
        this.kRply = kRply;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Host.serializer.serialize(sender, out);
            out.writeInt(k.size());
            for(Host neigh : k){
                Host.serializer.serialize(neigh, out);
            }
            out.writeInt(kRply.size());
            for(Host neigh : kRply){
                Host.serializer.serialize(neigh, out);
            }
        }

        @Override
        public ShuffleReply deserialize(ByteBuf in) throws IOException {
            Host sender = Host.serializer.deserialize(in);
            int c = in.readInt();
            HashSet<Host> k = new HashSet<>();
            for(int i = 0; i < c; i++){
                k.add(Host.serializer.deserialize((in)));
            }
            c = in.readInt();
            HashSet<Host> kRply = new HashSet<>();
            for(int i = 0; i < c; i++){
                kRply.add(Host.serializer.deserialize((in)));
            }

            return new ShuffleReply(sender, k, kRply);
        }
    };

    public Host getSender(){ return sender;}

    public HashSet<Host> getK(){ return k;}

    public HashSet<Host> getKRply(){ return kRply;}
    @Override
    public String toString() {
        return "Shuffle Reply";
    }
}
