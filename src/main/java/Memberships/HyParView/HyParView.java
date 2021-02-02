package Memberships.HyParView;

import Gossip.Gossip;
import Memberships.HyParView.Messages.*;
import Memberships.HyParView.Timers.ShuffleT;
import Memberships.HyParView.Timers.Views;
import Memberships.Membership;
import babel.core.GenericProtocol;
import babel.exceptions.HandlerRegistrationException;
import babel.generic.ProtoMessage;
import babel.handlers.ChannelEventHandler;
import channel.tcp.TCPChannel;
import channel.tcp.events.*;
import network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.*;

public class HyParView extends Membership {

    public static short PROTOCOL_ID = 100;
    public static String PROTOCOL_NAME = "HPV";

    Gossip gossip;

    Host myself, contact = null;
    int channelId;

    Set<Host> passiveView;
    Set<Host> potentialNeighbours;
    Set<Host> activeView;

    private int ACTIVE, PASSIVE, ARWL, PRWL, KAS, KPS, ShuffleTTL;
    private boolean shuffle = false;
    int hashStart;
    private static final Logger logger = LogManager.getLogger(HyParView.class);

    public HyParView(Gossip gossip) {
        super(PROTOCOL_NAME, PROTOCOL_ID);
        this.gossip = gossip;
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

            String  properties = "ACTIVE" + ACTIVE + "PASSIVE" + PASSIVE +
                    "ARWL" + ARWL + "PRWL" + PRWL +
                    "KAS"+ KAS + "KPS" + KPS + "ShuffleTTL" + ShuffleTTL;

            hashStart = properties.hashCode();

            activeView = new HashSet<>();
            passiveView = new HashSet();
            potentialNeighbours = new HashSet<>();

            Properties channelProps = new Properties();
            channelProps.setProperty(TCPChannel.ADDRESS_KEY, props.getProperty("address")); //The address to bind to
            channelProps.setProperty(TCPChannel.PORT_KEY, props.getProperty("port")); //The port to bind to
            channelProps.setProperty(TCPChannel.METRICS_INTERVAL_KEY, "10000"); //The interval to receive channel metrics
            channelProps.setProperty(TCPChannel.HEARTBEAT_INTERVAL_KEY, "1000"); //Heartbeats interval for established connections
            channelProps.setProperty(TCPChannel.HEARTBEAT_TOLERANCE_KEY, "3000"); //Time passed without heartbeats until closing a connection
            channelProps.setProperty(TCPChannel.CONNECT_TIMEOUT_KEY, "1000"); //TCP connect timeout
            channelId = createChannel(TCPChannel.NAME, channelProps); //Create the channel with the given properties

        } catch (IOException e) {
            e.printStackTrace();
        }


        registerMessageSerializer(channelId, Join.MSG_CODE, Join.serializer);
        registerMessageHandler(channelId, Join.MSG_CODE, this::uponJoin,
                this::uponJoinSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, Disconnect.MSG_CODE, Disconnect.serializer);
        registerMessageHandler(channelId, Disconnect.MSG_CODE, this::uponDisconnect,
                this::uponDisconnectSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, KillPill.MSG_CODE, KillPill.serializer);
        registerMessageHandler(channelId, KillPill.MSG_CODE, this::uponKillPill,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, ForwardJoin.MSG_CODE, ForwardJoin.serializer);
        registerMessageHandler(channelId, ForwardJoin.MSG_CODE, this::uponForwardJoin,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, JoinReply.MSG_CODE, JoinReply.serializer);
        registerMessageHandler(channelId, JoinReply.MSG_CODE, this::uponJoinReply,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, NeighbourReq.MSG_CODE, NeighbourReq.serializer);
        registerMessageHandler(channelId, NeighbourReq.MSG_CODE, this::uponNeighbourReq,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, NeighbourAcc.MSG_CODE, NeighbourAcc.serializer);
        registerMessageHandler(channelId, NeighbourAcc.MSG_CODE, this::uponNeighbourAcc,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, NeighbourRej.MSG_CODE, NeighbourRej.serializer);
        registerMessageHandler(channelId, NeighbourRej.MSG_CODE, this::uponNeighbourRej,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, FindNeighbour.MSG_CODE, FindNeighbour.serializer);
        registerMessageHandler(channelId, FindNeighbour.MSG_CODE, this::uponFindNeighbour,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, Shuffle.MSG_CODE, Shuffle.serializer);
        registerMessageHandler(channelId, Shuffle.MSG_CODE, this::uponShuffle,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, ShuffleReply.MSG_CODE, ShuffleReply.serializer);
        registerMessageHandler(channelId, ShuffleReply.MSG_CODE, this::uponShuffleReply,
                this::uponMessageSent, this::uponMessageFailed);

        registerMessageSerializer(channelId, ContactJoin.MSG_CODE, ContactJoin.serializer);
        registerMessageHandler(channelId, ContactJoin.MSG_CODE, this::uponContactJoin,
                this::uponMessageSent, this::uponMessageFailed);


        registerTimerHandler(Views.TIMER_CODE, this::uponViews);
        registerTimerHandler(ShuffleT.TIMER_CODE, this::uponShuffleTimer);


        registerChannelEventHandler(channelId, OutConnectionDown.EVENT_ID, this::uponOutConnectionDown);
        registerChannelEventHandler(channelId, InConnectionDown.EVENT_ID, this::uponInConnectionDown);



        if (props.containsKey("Contact")){
            try {
                String[] hostElements = props.getProperty("Contact").split(":");
                contact = new Host(InetAddress.getByName(hostElements[0]), Short.parseShort(hostElements[1]));

                System.out.println("LOGS-Open connection contact: " + contact);
                openConnection(contact);
                send(new Join(myself, hashStart), contact);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("LOGS-Invalid contact on configuration: '" + props.getProperty("Contact"));
            }
        }


        System.out.println("LOGS-Open connection myself: " + myself);
        openConnection(myself);
        send(new FindNeighbour(), myself);

        setupPeriodicTimer( new Views(), 1000, 1000);
        setupPeriodicTimer( new ShuffleT(), 5000, 1000);

    }

    protected void uponContactJoin(ContactJoin msg, Host from, short sProto, int cId) {
        contact = from;
        while (activeView.size() >= ACTIVE) {
            dropRandomFromActive(null);
        }
        activeView.add(from);
        System.out.println("LOGS-Open contact join: " + from);
    }

    protected void uponJoin(Join msg, Host from, short sProto, int cId) {
        int hash = msg.getHash();
        if(hash == hashStart) {
            while (activeView.size() >= ACTIVE) {
                dropRandomFromActive(null);
            }
            Host newNode = new Host(msg.getSender().getAddress(), msg.getSender().getPort());

            System.out.println("LOGS-Open connection upon join: " + newNode);
            openConnection(newNode);

            activeView.add(newNode);

            System.out.println("LOGS-join added new node" + newNode.toString());
            if (activeView.size() > 1) {
                for (Host neigh : activeView) {
                    if (!neigh.equals(newNode.toString())) {
                        send(new ForwardJoin(myself, newNode, ARWL), neigh);
                    }
                }
            }
        }else{
            send(new KillPill(myself, 0), msg.getSender());
        }

    }

    protected void uponKillPill(KillPill msg, Host from, short sProto, int cId) {
        if(msg.getCode() == 0) {
            if (contact != null) {
                if (msg.getSender().toString().equals(contact.toString())) {
                    System.out.println("LOGS-Kill pill, wrong arguments");
                    System.exit(0);
                }
            }
        }else if(msg.getCode() == 1){
            System.out.println("LOGS-Kill pill, leave from: " + from.toString());
            System.exit(0);
        }
    }

    protected void uponForwardJoin(ForwardJoin msg, Host from, short sProto, int cId) {
        if(!activeView.contains(msg.getSender())) {
            System.out.println("LOGS-received forward join from a node not in active " + msg.getSender());
            send(new Disconnect(myself), msg.getSender());
            return;
        }
        Host newNode =  new Host(msg.getNewNode().getAddress(), msg.getNewNode().getPort());
        Host sender = new Host(msg.getSender().getAddress(), msg.getNewNode().getPort());
        int ttl = msg.getTTL();
        if((activeView.size() <= 1 || ttl == 0) && !activeView.contains(newNode) && !newNode.equals(myself)){
            while (activeView.size() >= ACTIVE) {
                dropRandomFromActive(null);
            }

            System.out.println("LOGS-Open connection upon forward join: " + newNode);
            openConnection(newNode);

            activeView.add(newNode);
            passiveView.remove(newNode);
            potentialNeighbours.remove(newNode);

            JoinReply joinReply = new JoinReply(myself);

            System.out.println("LOGS-forwardJoin added: " + newNode.toString());

            send(joinReply, newNode);

        }else if((ttl == PRWL) && !activeView.contains(newNode) && !newNode.equals(myself)){
            if(passiveView.size() >= PASSIVE){
                dropRandomFromPassive();
            }

            passiveView.add(newNode);
        }

        if(msg.getTTL() > 0){
            Random r = new Random();
            HashSet<Host> tmp = new HashSet(activeView);

            tmp.remove(sender);
            tmp.remove(newNode);

            if(!tmp.isEmpty()){
                int i = r.nextInt(tmp.size());
                send(new ForwardJoin(myself, newNode, ttl - 1), (Host) tmp.toArray()[i]);
            }
        }
    }

    protected void uponJoinReply(JoinReply msg, Host from, short sProto, int cId) {
        Host sender = new Host(msg.getSender().getAddress(), msg.getSender().getPort());
        if(!activeView.contains(sender)) {
            while (activeView.size() >= ACTIVE) {
                dropRandomFromActive(null);
            }
            passiveView.remove(sender);
            potentialNeighbours.remove(sender);
            activeView.add(sender);

            System.out.println("LOGS-Open connection uponJoinReply: " + sender);
            openConnection(sender);

            System.out.println("LOGS-join reply added " + sender);
        }else{
            System.out.println("LOGS-Received joinReply message from someone in active; " + sender);
        }
    }

    protected void uponNeighbourReq(NeighbourReq msg, Host from, short sProto, int cId) {
        Host newNode = msg.getSender();
        int priority = msg.getPriority();
        if(!activeView.contains(newNode)) {
            if (activeView.size() >= ACTIVE && priority > 0) {
                while (activeView.size() >= ACTIVE) {
                    dropRandomFromActive(null);
                }
            }

            if (activeView.size() < ACTIVE) {

                activeView.add(newNode);
                passiveView.remove(newNode);
                potentialNeighbours.remove(newNode);

                NeighbourAcc neighbourAcc = new NeighbourAcc(myself);

                System.out.println("LOGS-neighbourReq added " + newNode);
                System.out.println("LOGS-Open connection upon neighbour Req: " + newNode);
                openConnection(newNode);
                send(neighbourAcc, newNode);

            } else {
                send(new NeighbourRej(myself), newNode);
            }
        }else{
            System.out.println("LOGS-Received neighbourReq message from someone in active; " + newNode);
        }
    }

    protected void uponNeighbourAcc(NeighbourAcc msg, Host from, short sProto, int cId) {
        Host newNode = new Host(msg.getSender().getAddress(), msg.getSender().getPort());
        if(!activeView.contains(newNode)) {
            while (activeView.size() >= ACTIVE) {
                dropRandomFromActive(null);
            }
            System.out.println("LOGS-Open connection uponNeighbour accept: " + newNode);
            openConnection(newNode);
            activeView.add(newNode);
            passiveView.remove(newNode);
            potentialNeighbours.remove(newNode);
            System.out.println("LOGS-neighbour accept added: " + newNode);
        }else{
            System.out.println("LOGS-Received accept message from someone in active; " + newNode);
        }
    }

    protected void uponNeighbourRej(NeighbourRej msg, Host from, short sProto, int cId) {
        send(new FindNeighbour(), myself);
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
            System.out.println("LOGS-Open connection Shuffle n: " + n);
            openConnection(n);
            send(new Shuffle(myself, myself, ShuffleTTL, k), n);
            uponViews(new Views(), 0);
        }
        if(activeView.size() == 0 && passiveView.size() == 0 && contact != null){
            System.out.println("LOGS-Open connection Shuffle contact: " + contact);
            openConnection(contact);
            send(new Join(myself, hashStart), contact);
        }
    }

    protected void uponShuffle(Shuffle msg, Host from, short sProto, int cId) {
        if(!activeView.contains(msg.getSender())) {
            System.out.println("LOGS-received shuffle from a node not in active " + msg.getSender());

            System.out.println("LOGS-Open connection upponShuffle sender: " + msg.getSender());
            openConnection(msg.getSender());
            send(new Disconnect(myself), msg.getSender());
            return;
        }
            int ttl = msg.getTTL() - 1;
            Host sender = new Host(msg.getSender().getAddress(), msg.getSender().getPort());
            Host origin = new Host(msg.getSender().getAddress(), msg.getSender().getPort());
            HashSet<Host> k = new HashSet<>(msg.getK());
            if (ttl > 0) {
                HashSet<Host> tmp = new HashSet(activeView);
                tmp.remove(sender);
                tmp.remove(origin);

                if (tmp.isEmpty()) {
                    return;
                } else {
                    Random rnd = new Random();
                    Host n = (Host) tmp.toArray()[rnd.nextInt(tmp.size())];
                    System.out.println("LOGS-Open connection upponShuffle n: " + n);
                    openConnection(n);
                    send(new Shuffle(myself, origin, ttl, k), n);
                }
            } else {
                HashSet<Host> kRply = new HashSet<>();
                Iterator<Host> it = passiveView.iterator();
                Host tmp;

                while (it.hasNext() && kRply.size() < k.size()) {
                    tmp = it.next();
                    if (!msg.getK().contains(tmp)) {
                        kRply.add(tmp);
                    }
                }

                System.out.println("LOGS-Open connection upponShiffle origin: " + sender);
                openConnection(origin);
                send(new ShuffleReply(myself, k, kRply), origin);
                it = k.iterator();
                Iterator<Host> it2 = kRply.iterator();
                while (it.hasNext()) {
                    tmp = it.next();
                    if (!passiveView.contains(tmp) && !activeView.contains(tmp) && !tmp.equals(myself)) {
                        if (passiveView.size() >= PASSIVE) {
                            if (it2.hasNext()) {
                                Host del = it2.next();
                                passiveView.remove(del);
                                potentialNeighbours.remove(del);
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
        HashSet<Host> k = new HashSet<>(msg.getK());
        HashSet<Host> kReply = new HashSet<>(msg.getKRply());

        it = k.iterator();
        it2 = kReply.iterator();
        Host tmp, tmp2;

        while(it.hasNext()){
            tmp = it.next();
            if(!passiveView.contains(tmp) && !activeView.contains(tmp) && !tmp.equals(myself)){
                if(passiveView.size() >= PASSIVE) {
                    if (it2.hasNext()) {
                        tmp2 = it2.next();
                        passiveView.remove(tmp2);
                        potentialNeighbours.remove(tmp2);
                    } else {
                        dropRandomFromPassive();
                    }
                }
                passiveView.add(tmp);
            }
        }

    }

    protected void uponDisconnect(Disconnect msg, Host from, short sProto, int cId) {
        Host del = new Host(msg.getSender().getAddress(), msg.getSender().getPort());
        activeView.remove(del);
        passiveView.add(del);
        System.out.println("LOGS-disconnect from: " + del);

        close(del);
    }

    protected void uponFindNeighbour(FindNeighbour msg, Host from, short sProto, int cId) {
        if(activeView.size() == ACTIVE) {
            return;
        } else if(potentialNeighbours.isEmpty()){
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

        while(!potentialNeighbours.isEmpty() && c > 0) {
            int i = rnd.nextInt(potentialNeighbours.size());
            h = (Host) potentialNeighbours.toArray()[i];
            System.out.println("LOGS-sending neighbour request to " + h);
            System.out.println("LOGS-Open connection upon find neighbour: " + h);
            openConnection(h);
            send(new NeighbourReq(myself,priority), h);
            potentialNeighbours.remove(h);
            priority--;
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

        Disconnect dc = new Disconnect(myself);
        System.out.println("LOGS-removed: " + del.toString());
        send(dc, del);
    }

    private  void dropRandomFromPassive(){
        Random rnd = new Random();
        int i = rnd.nextInt(passiveView.size());
        Host del = (Host) passiveView.toArray()[i];
        passiveView.remove(del);
        potentialNeighbours.remove(del);
    }

    protected void uponJoinSent(ProtoMessage msg, Host to, short destProto, int channelId){
        activeView.add(to);
        System.out.println("LOGS-added contact: " + to.toString());
    }

    protected void uponDisconnectSent(ProtoMessage msg, Host to, short destProto, int channelId){ ;
        //closeConnection(to);
    }

    protected void uponMessageSent(ProtoMessage msg, Host to, short destProto, int channelId){
        //System.out.println("Sent: " + msg.toString() + " to " + to.toString());
    }
    protected void uponMessageFailed(ProtoMessage msg, Host to, short destProto, Throwable cause, int channelId) {
        activeView.remove(to);
        Disconnect dc = new Disconnect(myself);
        System.out.println("LOGS-Open connection uppon message failed: " + to);
        openConnection(to);
        send(dc, to);
        System.out.println("LOGS-Message Failed: " + to.toString() + msg.toString());
    }

    private void uponInConnectionDown(InConnectionDown evt, int channelId) {
        //System.out.println("Disconnect: " + evt.getNode().toString());
        activeView.remove(evt.getNode());
        System.out.println("LOGS-node down :" + evt.getNode().toString());
    }

    private void uponOutConnectionDown(OutConnectionDown evt, int channelId) {
        //System.out.println("Disconnect: " + evt.getNode().toString());
        activeView.remove(evt.getNode());
        System.out.println("LOGS-node down :" + evt.getNode().toString());
    }


    private void send(ProtoMessage msg, Host to){
        try {
            sendMessage(msg, to);
        }catch (java.util.concurrent.RejectedExecutionException e) {
            System.out.println("LOGS-nodeDown");
            activeView = new HashSet<>();
            printViews();
            printViews();
            printViews();
            printViews();
            printViews();
            System.exit(1);
        }
    }

    private void close(Host del){
        try {
            closeConnection(del);
        }catch (java.util.concurrent.RejectedExecutionException e) {
            System.out.println("LOGS-nodeDown");
            activeView = new HashSet<>();
            printViews();
            printViews();
            printViews();
            printViews();
            printViews();
            System.exit(1);
        }
    }

    private void uponViews(Views timer, long uId) {
        printViews();
        potentialNeighbours = new HashSet<>(passiveView);
        if(activeView.size() < ACTIVE && !passiveView.isEmpty()) {
            potentialNeighbours = new HashSet<>(passiveView);
            send(new FindNeighbour(), myself);
        }
        /**
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
         **/
    }

    private void printViews() {
        Iterator<Host> active = activeView.iterator();
        Host next;
        String debug = "LOGS,";
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
    }

    @Override
    public void join(String ip, int port) throws UnknownHostException {
        Host newNode = new Host(InetAddress.getByName(ip), port);

        System.out.println("LOGS-Open connection upon join: " + newNode);
        openConnection(newNode);

        activeView.add(newNode);

        send(new ContactJoin(myself), newNode);
        System.out.println("LOGS-join added new node" + newNode.toString());
        if (activeView.size() > 1) {
            for (Host neigh : activeView) {
                if (!neigh.equals(newNode.toString())) {
                    send(new ForwardJoin(myself, newNode, ARWL), neigh);
                }
            }
        }
    }

    public String members() {
        Iterator<Host> active = activeView.iterator();
        Host next;
        String members = "MEMBERS: ";
        members += new Timestamp(System.currentTimeMillis()) +", ";
        for(int i = 0; i<ACTIVE;i++){
            if (active.hasNext()) {
                next = active.next();
                members += next.getAddress() + "-";
                members += next.getPort();
            }
            else{
                members +="-1";
            }
            if(i<ACTIVE - 1){
                members += ", ";
            }
        }

        return members;
    }

    @Override
    public void leave() {
        System.out.println("LOGS-Self leave");
        System.exit(0);
    }

    @Override
    public void leave(String ip, int port) throws UnknownHostException {
        Host del = new Host(InetAddress.getByName(ip), port);

        System.out.println("LOGS-Open connection upon join: " + del);
        openConnection(del);

        send(new KillPill(myself, 1), del);
    }
}
