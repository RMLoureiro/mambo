package Gossip.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DirectMessage extends ProtoMessage {
    private final String message;
    public static final short MSG_CODE = 001;

    public DirectMessage(String message) {
        super(MSG_CODE);
        this.message = message;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            DirectMessage msg = (DirectMessage) message;
            byte[] messageString = msg.getMessage().getBytes(StandardCharsets.UTF_8);
            out.writeInt(messageString.length);
            out.writeBytes(messageString);
        }

        @Override
        public DirectMessage deserialize(ByteBuf in) throws IOException {
            int size = in.readInt();
            byte[] messageString = new byte[size];
            in.getBytes(size, messageString);
            String message = new String(messageString);
            return new DirectMessage(message);
        }
    };

    public String getMessage() { return message; }
    @Override
    public String toString() {
        return "Gossip message " + message;
    }
}
