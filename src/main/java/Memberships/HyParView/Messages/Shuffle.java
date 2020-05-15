package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class Shuffle extends ProtoMessage {

    private final Host sender, origin;
    public static final short MSG_CODE = 108;
    private final int TTL;
    private final HashSet<Host> k;

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
            Shuffle msg = (Shuffle) message;
            Host.serializer.serialize(msg.sender, out);
            Host.serializer.serialize(msg.origin, out);
            out.writeInt(msg.TTL);
            out.writeInt(msg.k.size());
            for(Host neigh : msg.k){
                Host.serializer.serialize(neigh, out);
            }
        }

        @Override
        public Shuffle deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            Host starter = Host.serializer.deserialize(in);
            int ttl = in.readInt();
            int c = in.readInt();
            HashSet<Host> set = new HashSet<>();
            for(int i = 0; i < c; i++){
                set.add(Host.serializer.deserialize((in)));
            }
            return new Shuffle(host, starter, ttl, set);
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
