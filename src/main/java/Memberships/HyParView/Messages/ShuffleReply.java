package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class ShuffleReply extends ProtoMessage {

    private final Host sender;
    public static final short MSG_CODE = 109;
    private final HashSet<Host> k, kRply;

    public ShuffleReply(Host sender, HashSet<Host> k, HashSet<Host> kRply) {
        super(MSG_CODE);
        this.sender = sender;
        this.k = k;
        this.kRply = kRply;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            ShuffleReply msg = (ShuffleReply) message;
            Host.serializer.serialize(msg.sender, out);
            out.writeInt(msg.k.size());
            for(Host neigh : msg.k){
                Host.serializer.serialize(neigh, out);
            }
            out.writeInt(msg.kRply.size());
            for(Host neigh : msg.kRply){
                Host.serializer.serialize(neigh, out);
            }
        }

        @Override
        public ShuffleReply deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int c = in.readInt();
            HashSet<Host> set = new HashSet<>();
            for(int i = 0; i < c; i++){
                set.add(Host.serializer.deserialize((in)));
            }
            c = in.readInt();
            HashSet<Host> setRply = new HashSet<>();
            for(int i = 0; i < c; i++){
                setRply.add(Host.serializer.deserialize((in)));
            }

            return new ShuffleReply(host, set, setRply);
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
