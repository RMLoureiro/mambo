package Memberships.HyParView;

import Memberships.HyParView.Messages.*;
import Memberships.HyParView.Timer.ShuffleT;
import Memberships.HyParView.Timer.Views;
import babel.exceptions.HandlerRegistrationException;
import babel.generic.GenericProtocol;
import babel.generic.ProtoMessage;
import channel.ackos.events.NodeDownEvent;
import network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.*;

public class HyParView extends GenericProtocol {

    public static short PROTOCOL_ID = 100;
    public static String PROTOCOL_NAME = "HPV";

    Host myself;
    int channelId;

    Set<Host> passiveView;
    Set<Host> activeView;
    private int ACTIVE, PASSIVE, ARWL, PRWL, KAS, KPS, ShuffleTTL;
    int hash;
    private static final Logger logger = LogManager.getLogger(HyParView.class);

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
            KAS = Integer.parseInt(props.getProperty("KAS"));
            KPS = Integer.parseInt(props.getProperty("KPS"));
            ShuffleTTL = Integer.parseInt(props.getProperty("ShuffleTTL"));

            String  properties = "ACTIVE" + ACTIVE + "PASSIVE" + PASSIVE + "ARWL" + ARWL + "PRWL" + PRWL +
                    "KAS"+ KAS + "KPS" + KPS + "ShuffleTTL" + ShuffleTTL;

            hash = properties.hashCode();

            activeView = new HashSet<>();
            passiveView = new HashSet();

            //System.out.println(InetAddress.getByName(props.getProperty("address")) + props.getProperty("port"));
            channelId = createChannel("Ackos", props);
        } catch (IOException e) {
            e.printStackTrace();
        }


        registerMessageSerializer(Join.MSG_CODE, Join.serializer);
        registerMessageHandler(channelId, Join.MSG_CODE, this::uponJoin,
                this::uponJoinSent, this::uponMessageFailed);

        registerMessageSerializer(ForwardJoin.MSG_CODE, ForwardJoin.serializer);
        registerMessageHandler(channelId, ForwardJoin.MSG_CODE, this::uponForwardJoin,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(JoinReply.MSG_CODE, JoinReply.serializer);
        registerMessageHandler(channelId, JoinReply.MSG_CODE, this::uponJoinReply,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(NeighbourReq.MSG_CODE, NeighbourReq.serializer);
        registerMessageHandler(channelId, NeighbourReq.MSG_CODE, this::uponNeighbourReq,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(NeighbourAcc.MSG_CODE, NeighbourAcc.serializer);
        registerMessageHandler(channelId, NeighbourAcc.MSG_CODE, this::uponNeighbourAcc,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(NeighbourRej.MSG_CODE, NeighbourRej.serializer);
        registerMessageHandler(channelId, NeighbourRej.MSG_CODE, this::uponNeighbourRej,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(FindNeighbour.MSG_CODE, FindNeighbour.serializer);
        registerMessageHandler(channelId, FindNeighbour.MSG_CODE, this::uponFindNeighbour,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(Shuffle.MSG_CODE, Shuffle.serializer);
        registerMessageHandler(channelId, Shuffle.MSG_CODE, this::uponShuffle,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(ShuffleReply.MSG_CODE, ShuffleReply.serializer);
        registerMessageHandler(channelId, ShuffleReply.MSG_CODE, this::uponShuffleReply,
                this::uponMessageSent, this::uponMessageFailed);

        registerTimerHandler(Views.TIMER_CODE, this::uponViews);
        registerTimerHandler(ShuffleT.TIMER_CODE, this::uponShuffleTimer);

        registerChannelEventHandler(channelId, NodeDownEvent.EVENT_ID, this::uponNodeDown);

        if (props.containsKey("Contact")){
            try {
                String[] hostElements = props.getProperty("Contact").split(":");
                Host contact = new Host(InetAddress.getByName(hostElements[0]), Short.parseShort(hostElements[1]));
                //System.out.println(myself.toString());
                sendMessage(new Join(myself), contact);
                sendMessage(new FindNeighbour(), new Host(myself.getAddress(), myself.getPort()));
            } catch (Exception e) {
                e.printStackTrace();
                //System.out.println("Invalid contact on configuration: '" + props.getProperty("Contact"));
            }
        }


        sendMessage(new FindNeighbour(), new Host(myself.getAddress(), myself.getPort()));

        setupPeriodicTimer( new Views(), 1000, 1000);
        setupPeriodicTimer( new ShuffleT(), 5000, 5000);
    }

    protected void uponJoin(Join msg, Host from, short sProto, int cId) {
        //System.out.println("Received: " + msg.toString() + " from " + msg.getSender().toString());

        while(activeView.size() >= ACTIVE){
            dropRandomFromActive(null);
        }

        activeView.add(new Host(msg.getSender().getAddress(), msg.getSender().getPort()));

        if(activeView.size() > 1){
            for(Host neigh : activeView){
                if(!neigh.equals(msg.getSender().toString())){
                    sendMessage(new ForwardJoin(myself, msg.getSender(), ARWL),  neigh);

                }
            }
        }

    }

    protected void uponForwardJoin(ForwardJoin msg, Host from, short sProto, int cId) {
        //System.out.println("Received: " + msg.toString() + " from " + msg.getSender().toString());
        if((activeView.size() <= 1 || msg.getTTL() == 0) && !activeView.contains(msg.getNewNode()) && !msg.getNewNode().equals(myself)){
            while (activeView.size() >= ACTIVE) {
                dropRandomFromActive(null);
            }

            passiveView.remove(msg.getNewNode());
            activeView.add( new Host(msg.getNewNode().getAddress(), msg.getNewNode().getPort()));
            sendMessage(new JoinReply(myself), msg.getNewNode());

        }else if((msg.getTTL() == PRWL) && !activeView.contains(msg.getNewNode()) && !msg.getNewNode().equals(myself)){
            if(passiveView.size() >= PASSIVE){
                dropRandomFromPassive();
            }

            passiveView.add(msg.getNewNode());
        }

        if(msg.getTTL() > 0){
            Random r = new Random();
            HashSet<Host> tmp = new HashSet(activeView);

            tmp.remove(msg.getSender());
            tmp.remove(msg.getNewNode());

            if(!tmp.isEmpty()){
                int i = r.nextInt(tmp.size());
                sendMessage(new ForwardJoin(myself, msg.getNewNode(), msg.getTTL() - 1), (Host) tmp.toArray()[i]);
            }
        }
    }

    protected void uponJoinReply(JoinReply msg, Host from, short sProto, int cId) {
        //System.out.println("Received: " + msg.toString() + " from " + msg.getSender().toString());
        while(activeView.size() >= ACTIVE){
            dropRandomFromActive(null);
        }
        passiveView.remove(msg.getSender());
        activeView.add(msg.getSender());
    }

    protected void uponNeighbourReq(NeighbourReq msg, Host from, short sProto, int cId) {
        if(!activeView.contains(msg.getSender())) {
            if (activeView.size() >= ACTIVE && msg.getPriority() > 0) {
                while (activeView.size() >= ACTIVE) {
                    dropRandomFromActive(null);
                }
            }

            if (activeView.size() < ACTIVE) {
                activeView.add(msg.getSender());
                passiveView.remove(msg.getSender());
                sendMessage(new NeighbourAcc(myself), msg.getSender());
            } else {
                sendMessage(new NeighbourRej(myself), msg.getSender());
            }
        }
    }

    protected void uponNeighbourAcc(NeighbourAcc msg, Host from, short sProto, int cId) {
        activeView.add(msg.getSender());
        passiveView.remove(msg.getSender());
    }

    protected void uponNeighbourRej(NeighbourRej msg, Host from, short sProto, int cId) {
        sendMessage(new FindNeighbour(), new Host(myself.getAddress(), myself.getPort()));
    }


    private void uponShuffleTimer(ShuffleT timer, long uId) {
        if(!activeView.isEmpty()) {
            Random rnd = new Random();
            Host n = (Host) activeView.toArray()[rnd.nextInt(activeView.size())];

            HashSet<Host> k = new HashSet<>(activeView);
            HashSet<Host> kp = new HashSet<>(passiveView);

            while (k.size() > KAS) {
                k.remove(k.toArray()[rnd.nextInt(k.size())]);
            }

            while (kp.size() > KPS) {
                kp.remove(kp.toArray()[rnd.nextInt(kp.size())]);
            }

            k.addAll(kp);
            sendMessage(new Shuffle(myself, myself, ShuffleTTL, k), n);
        }
    }

    protected void uponShuffle(Shuffle msg, Host from, short sProto, int cId) {
        int ttl = msg.getTTL() - 1;

        if(ttl > 0){
            HashSet<Host> tmp = new HashSet(activeView);
            tmp.remove(msg.getSender());
            tmp.remove(msg.getOrigin());

            if(tmp.isEmpty()){
                return;
            }else{
                Random rnd = new Random();
                Host n = (Host) tmp.toArray()[rnd.nextInt(tmp.size())];
                sendMessage(new Shuffle(myself, msg.getOrigin(), ttl, msg.getK()), n);
            }
        }else{
            HashSet<Host> kRply = new HashSet<>();
            Iterator<Host> it = passiveView.iterator();
            Host tmp;

            while(it.hasNext() && kRply.size() < msg.getK().size()){
                tmp = it.next();
                if(!msg.getK().contains(tmp)){
                    kRply.add(tmp);
                }
            }

            sendMessage(new ShuffleReply(myself, msg.getK(), kRply), msg.getOrigin());

            it = msg.getK().iterator();
            Iterator<Host> it2 = kRply.iterator();
            while (it.hasNext()) {
                tmp = it.next();
                if (!passiveView.contains(tmp) && !activeView.contains(tmp) && !tmp.equals(myself)) {
                    if (passiveView.size() >= PASSIVE) {
                        if (it2.hasNext()) {
                            passiveView.remove(it2.next());
                        } else {
                            dropRandomFromPassive();
                        }
                    }
                    passiveView.add(tmp);
                }
            }
        }
    }

    protected void uponShuffleReply(ShuffleReply msg, Host from, short sProto, int cId) {
        Iterator<Host> it, it2;
        it = msg.getKRply().iterator();
        it2 = msg.getK().iterator();
        Host tmp, tmp2;

        while(it.hasNext()){
            tmp = it.next();
            if(!passiveView.contains(tmp) && !activeView.contains(tmp) && !tmp.equals(myself)){
                if(passiveView.size() >= PASSIVE) {
                    if (it2.hasNext()) {
                        tmp2 = it2.next();
                        passiveView.remove(tmp2);
                    } else {
                        dropRandomFromPassive();
                    }
                }
                passiveView.add(tmp);
            }
        }

    }

    protected void uponFindNeighbour(FindNeighbour msg, Host from, short sProto, int cId) {
        if(activeView.size() == ACTIVE) {
            return;
        } else if(passiveView.isEmpty()){
            sendMessage(new FindNeighbour(), new Host(myself.getAddress(), myself.getPort()));
            return;
        }

        int priority;

        if(activeView.size() <= 1){
            priority = 1 + activeView.size();
        }else{
            priority = 0;
        }

        Random rnd = new Random();
        int c = ACTIVE - activeView.size();
        Host h;

        while(!passiveView.isEmpty() && c > 0) {
            int i = rnd.nextInt(passiveView.size());
            h = (Host) passiveView.toArray()[i];
            sendMessage(new NeighbourReq(myself,priority), h);
            passiveView.remove(h);
            priority--;
        }

        if(c > 0){
            sendMessage(new FindNeighbour(), new Host(myself.getAddress(), myself.getPort()));
        }
    }


    private void dropRandomFromActive(String addr) {
        Random rnd = new Random();
        HashSet<Host> tmp = new HashSet(activeView);

        if(addr != null){
            tmp.remove(addr);
        }

        int i = rnd.nextInt(tmp.size());
        Host del = (Host) tmp.toArray()[i];
        activeView.remove(del);
        passiveView.add(del);

        closeConnection(del);
    }

    private  void dropRandomFromPassive(){
        Random rnd = new Random();
        int i = rnd.nextInt(passiveView.size());
        String del = (String) passiveView.toArray()[i];
        passiveView.remove(del);
    }

    protected void uponJoinSent(ProtoMessage msg, Host to, short destProto, int channelId){
        //System.out.println("Sent: " + msg.toString() + " to " + to.toString());
        activeView.add(to);
    }

    protected void uponMessageSent(ProtoMessage msg, Host to, short destProto, int channelId){
        //System.out.println("Sent: " + msg.toString() + " to " + to.toString());
    }
    protected void uponMessageFailed(ProtoMessage msg, Host to, short destProto, Throwable cause, int channelId) {
        activeView.remove(to.toString());
        closeConnection(to);
        System.out.println("Message Failed: " + to.toString());
    }

    private void uponNodeDown(NodeDownEvent<ProtoMessage> evt, int channelId) {
        //System.out.println("Disconnect: " + evt.getNode().toString());
        activeView.remove(evt.getNode().toString());
        closeConnection(evt.getNode());
    }


    private void uponViews(Views timer, long uId) {
        Iterator<Host> active = activeView.iterator();
        Host next;
        String debug = "";
       debug += new Timestamp(System.currentTimeMillis()) +", ";
        for(int i = 0; i<ACTIVE;i++){
            if (active.hasNext()) {
                next = active.next();
                debug += next.getAddress() + "-";
                debug += next.getPort();
            }
            else{
                debug +="-1";
            }
            if(i<ACTIVE - 1){
                debug += ", ";
            }
        }

        System.out.println(debug);

        debug = "";
        Iterator<Host> passive = passiveView.iterator();
         for(int i = 0; i<PASSIVE;i++) {
             if (passive.hasNext()) {
                 next = passive.next();
                 debug += next.getAddress().toString() + ":";
                 debug += next.getPort();
             } else {
                 debug += "-1";
             }
             if (i < PASSIVE - 1) {
                 debug += ", ";
             }
         }
        System.out.println(debug);
    }
}
