package Memberships.HyParView;

import Memberships.HyParView.Messages.Join;
import babel.exceptions.HandlerRegistrationException;
import babel.generic.GenericProtocol;
import network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class HyParView extends GenericProtocol {

    public static short PROTOCOL_ID = 1;
    public static String PROTOCOL_NAME = "HPV";

    Host myself;
    int channelId;


    public HyParView() {
        super(PROTOCOL_NAME, PROTOCOL_ID);
    }


    public void init(Properties props) throws HandlerRegistrationException {
        try {
            myself = new Host(InetAddress.getByName(props.getProperty("address")),
                    Integer.parseInt(props.getProperty("port")));
            System.out.println(InetAddress.getByName(props.getProperty("address")) + props.getProperty("port"));
            channelId = createChannel("Ackos", props);
        } catch (IOException e) {
            e.printStackTrace();
        }


        registerMessageSerializer(Join.MSG_CODE, Join.serializer);
        registerMessageHandler(channelId, Join.MSG_CODE, this::uponJoin,
                this::uponJoinSent, this::uponMessageFailed);

        if (props.containsKey("Contact")){
            try {
                String[] hostElements = props.getProperty("Contact").split(":");
                Host contact = new Host(InetAddress.getByName(hostElements[0]), Short.parseShort(hostElements[1]));
                System.out.println(myself.toString());
                sendMessage(new Join(myself), contact);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Invalid contact on configuration: '" + props.getProperty("Contact"));
            }
        }


    }

    protected void uponJoin(Join msg, Host from, short sProto, int cId) {
        System.out.println("Received: " +  msg.toString() + " from " + msg.getSender().toString());

    }

    protected void uponJoinSent(Join msg, Host to, short destProto, int channelId){
        System.out.println("Sent: " + msg.toString() + " to " + to.toString());
    }
    protected void uponMessageFailed(Join msg, Host to, short destProto, Throwable cause, int channelId) {

    }
}
