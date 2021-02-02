package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class KillPill extends ProtoMessage {
    private final Host sender;
    private final int code;
    public static final short MSG_CODE = 110;

    public KillPill(Host sender, int code) {
        super(MSG_CODE);
        this.sender = sender;
        this.code = code;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            KillPill msg = (KillPill) message;
            Host.serializer.serialize(msg.sender, out);
            out.writeInt(msg.code);
        }

        @Override
        public KillPill deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            int code =  in.readInt();
            return new KillPill(host, code);
        }
    };

    public Host getSender(){ return sender;}

    public int getCode(){ return code; }
    @Override
    public String toString() {
        return "Kill Pill Message";
    }
}
