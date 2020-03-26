package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class Shuffle extends ProtoMessage {

    static Host sender, origin;
    public static final short MSG_CODE = 108;
    static int TTL;
    static HashSet<Host> k;

    public Shuffle(Host sender, Host origin, int TTL, HashSet<Host> k) {
        super(MSG_CODE);
        this.sender = sender;
        this.k = k;
        this.TTL = TTL;
        this.origin = origin;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            Host.serializer.serialize(sender, out);
            Host.serializer.serialize(origin, out);
            out.writeInt(TTL);
            out.writeInt(k.size());
            for(Host neigh : k){
                Host.serializer.serialize(neigh, out);
            }
        }

        @Override
        public Shuffle deserialize(ByteBuf in) throws IOException {
            Host sender = Host.serializer.deserialize(in);
            Host origin = Host.serializer.deserialize(in);
            int TTL = in.readInt();
            int c = in.readInt();
            HashSet<Host> k = new HashSet<>();
            for(int i = 0; i < c; i++){
                k.add(Host.serializer.deserialize((in)));
            }
            return new Shuffle(sender, origin, TTL, k);
        }
    };

    public Host getSender(){ return sender;}

    public int getTTL(){ return TTL;}

    public HashSet<Host> getK(){ return k;}

    public Host getOrigin(){ return origin;}

    @Override
    public String toString() {
        return "Shuffle Message with TTL of value " + TTL;
    }
}
