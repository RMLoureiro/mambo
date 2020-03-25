package Memberships.HyParView;

import Memberships.HyParView.Messages.ForwardJoin;
import Memberships.HyParView.Messages.Join;
import Memberships.HyParView.Messages.JoinReply;
import babel.exceptions.HandlerRegistrationException;
import babel.generic.GenericProtocol;
import babel.generic.ProtoMessage;
import channel.ackos.events.NodeDownEvent;
import channel.tcp.events.InConnectionDown;
import channel.tcp.events.OutConnectionDown;
import network.data.Host;
import network.pipeline.InConnectionHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

public class HyParView extends GenericProtocol {

    public static short PROTOCOL_ID = 100;
    public static String PROTOCOL_NAME = "HPV";

    Host myself;
    int channelId;

    Set<Host> passiveView;
    Map<String, Host> activeView;
    private int ACTIVE, PASSIVE, ARWL, PRWL;


    public HyParView() {
        super(PROTOCOL_NAME, PROTOCOL_ID);
    }


    public void init(Properties props) throws HandlerRegistrationException {
        try {
            myself = new Host(InetAddress.getByName(props.getProperty("address")),
                    Integer.parseInt(props.getProperty("port")));

            ACTIVE = Integer.parseInt(props.getProperty("active"));
            PASSIVE = Integer.parseInt(props.getProperty("passive"));
            ARWL = Integer.parseInt(props.getProperty("ARWL"));
            PRWL = Integer.parseInt(props.getProperty("PRWL"));

            activeView = new HashMap<>();
            passiveView = new HashSet();

            System.out.println(InetAddress.getByName(props.getProperty("address")) + props.getProperty("port"));
            channelId = createChannel("Ackos", props);
        } catch (IOException e) {
            e.printStackTrace();
        }


        registerMessageSerializer(Join.MSG_CODE, Join.serializer);
        registerMessageHandler(channelId, Join.MSG_CODE, this::uponJoin,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(ForwardJoin.MSG_CODE, ForwardJoin.serializer);
        registerMessageHandler(channelId, ForwardJoin.MSG_CODE, this::uponForwardJoin,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(JoinReply.MSG_CODE, JoinReply.serializer);
        registerMessageHandler(channelId, JoinReply.MSG_CODE, this::uponJoinReply,
                this::uponMessageSent, this::uponMessageFailed);

        registerChannelEventHandler(channelId, NodeDownEvent.EVENT_ID, this::uponNodeDown);

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
        System.out.println("Received: " + msg.toString() + " from " + msg.getSender().toString());

        while(activeView.size() >= ACTIVE){
            dropRandomFromActive(null);
        }

        activeView.put(msg.getSender().toString(), new Host(msg.getSender().getAddress(), msg.getSender().getPort()));

        if(activeView.size() > 1){
            for(String neigh : activeView.keySet()){
                if(!neigh.equals(msg.getSender().toString())){
                    sendMessage(new ForwardJoin(myself, msg.getSender(), ARWL),  activeView.get(neigh));
                }
            }
        }

    }

    protected void uponForwardJoin(ForwardJoin msg, Host from, short sProto, int cId) {
        System.out.println("Received: " + msg.toString() + " from " + msg.getSender().toString());
        if((activeView.size() <= 1 || msg.getTTL() == 0) && !activeView.containsKey(msg.getNewNode().toString()) && !msg.getNewNode().equals(myself)){
            while (activeView.size() >= ACTIVE) {
                dropRandomFromActive(null);
            }

            passiveView.remove(msg.getNewNode());
            activeView.put(msg.getNewNode().toString(), new Host(msg.getNewNode().getAddress(), msg.getNewNode().getPort()));
            sendMessage(new JoinReply(myself), msg.getNewNode());

        }else if((msg.getTTL() == PRWL) && !activeView.containsKey(msg.getNewNode().toString()) && !msg.getNewNode().equals(myself)){
            if(passiveView.size() >= PASSIVE){
                dropRandomFromPassive();
            }

            passiveView.add(msg.getNewNode());
        }

        if(msg.getTTL() > 0){
            Random r = new Random();
            HashSet<String> tmp = new HashSet<>(activeView.keySet());

            tmp.remove(msg.getSender());
            tmp.remove(msg.getNewNode());

            if(!tmp.isEmpty()){
                int i = r.nextInt(tmp.size());
                sendMessage(new ForwardJoin(myself, msg.getNewNode(), msg.getTTL() - 1), activeView.get(tmp.toArray()[i]));
            }
        }
    }

    protected void uponJoinReply(JoinReply msg, Host from, short sProto, int cId) {
        System.out.println("Received: " + msg.toString() + " from " + msg.getSender().toString());
        while(activeView.size() >= ACTIVE){
            dropRandomFromActive(null);
        }
        passiveView.remove(msg.getSender());
        activeView.put(msg.getSender().toString(), msg.getSender());
    }

    private void dropRandomFromActive(String addr) {
        Random rnd = new Random();
        HashSet<String> tmp = new HashSet<>(activeView.keySet());

        if(addr != null){
            tmp.remove(addr);
        }

        int i = rnd.nextInt(tmp.size());
        String del = (String) tmp.toArray()[i];
        Host disc = activeView.remove(del);

        closeConnection(disc);
    }

    private  void dropRandomFromPassive(){
        Random rnd = new Random();
        int i = rnd.nextInt(passiveView.size());
        String del = (String) passiveView.toArray()[i];
        passiveView.remove(del);
    }

    protected void uponMessageSent(ProtoMessage msg, Host to, short destProto, int channelId){
        System.out.println("Sent: " + msg.toString() + " to " + to.toString());
    }
    protected void uponMessageFailed(ProtoMessage msg, Host to, short destProto, Throwable cause, int channelId) {
        activeView.remove(to.toString());
        closeConnection(to);
        System.out.println("Message Failed: " + to.toString());
    }

    private void uponNodeDown(NodeDownEvent<ProtoMessage> evt, int channelId) {
        System.out.println("Disconnect: " + evt.getNode().toString());
        activeView.remove(evt.getNode().toString());
        closeConnection(evt.getNode());
    }
}
