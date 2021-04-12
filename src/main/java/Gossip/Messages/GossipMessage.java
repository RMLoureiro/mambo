package Gossip.Messages;

import babel.generic.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GossipMessage extends ProtoMessage {
    private final String message;
    private final int id;
    public static final short MSG_CODE = 002;

    public GossipMessage(int id, String message) {
        super(MSG_CODE);
        this.message = message;
        this.id = id;
    }

    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {
            GossipMessage msg = (GossipMessage) message;
            out.writeInt(msg.getMessageId());
            byte[] messageString = msg.getMessage().getBytes(StandardCharsets.UTF_8);
            out.writeInt(messageString.length);
            out.writeBytes(messageString);
        }

        @Override
        public GossipMessage deserialize(ByteBuf in) throws IOException {
            int id = in.readInt();
            int size = in.readInt();
            byte[] messageString = new byte[size];
            in.getBytes(size, messageString);
            String message = new String(messageString);
            return new GossipMessage(id, message);
        }
    };

    public String getMessage() { return message; }

    public int getMessageId(){ return id; }
    @Override
    public String toString() {
        return "Gossip message " + message;
    }
}
