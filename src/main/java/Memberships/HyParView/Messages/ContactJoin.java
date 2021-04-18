package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class ContactJoin extends ProtoMessage{
    private final Host sender;
    public static final short MSG_CODE = 112;

    public ContactJoin(Host sender) {
        super(MSG_CODE);
        this.sender = sender;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            ContactJoin msg = (ContactJoin) message;
            Host.serializer.serialize(msg.sender, out);
        }

        @Override
        public ContactJoin deserialize(ByteBuf in) throws IOException {
            Host host = Host.serializer.deserialize(in);
            return new ContactJoin(host);
        }
    };

    public Host getSender(){ return sender;}

    @Override
    public String toString() {
        return "Join reply Message";
    }
}
