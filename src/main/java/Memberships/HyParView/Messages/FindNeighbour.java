package Memberships.HyParView.Messages;

import babel.generic.ProtoMessage;
import babel.generic.ProtoNotification;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class FindNeighbour extends ProtoMessage {
    public final static short MSG_CODE = 105;

    public FindNeighbour() {
        super(FindNeighbour.MSG_CODE);
    }


    public static final ISerializer<ProtoMessage> serializer = new ISerializer<ProtoMessage>() {
        @Override
        public void serialize(ProtoMessage message, ByteBuf out) throws IOException {

        }

        @Override
        public FindNeighbour deserialize(ByteBuf in) throws IOException {
            return new FindNeighbour();
        }
    };

    @Override
    public String toString() {
        return "Find new Neighbour";
    }
}
