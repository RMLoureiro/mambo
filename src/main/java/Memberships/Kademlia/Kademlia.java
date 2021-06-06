package Memberships.Kademlia;

import Gossip.Gossip;
import Memberships.Membership;
import babel.exceptions.HandlerRegistrationException;
import channel.tcp.TCPChannel;
import network.data.Host;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Set;

public class Kademlia extends Membership {

    public static short PROTOCOL_ID = 101;
    public static String PROTOCOL_NAME = "KAD";

    Gossip gossip;

    String id;
    Host myself;
    int channelId;

    public Kademlia(Gossip gossip) {
        super(PROTOCOL_NAME, PROTOCOL_ID);
        this.gossip = gossip;
    }

    @Override
    public void init(Properties props) throws HandlerRegistrationException, IOException {
        myself = new Host(InetAddress.getByName(props.getProperty("address")),
                Integer.parseInt(props.getProperty("port")));

        id = new BigInteger( DigestUtils.sha1Hex(myself.getAddress() + ":" + myself.getPort()), 16).toString(2);

        Properties channelProps = new Properties();
        channelProps.setProperty(TCPChannel.ADDRESS_KEY, props.getProperty("address")); //The address to bind to
        channelProps.setProperty(TCPChannel.PORT_KEY, props.getProperty("port")); //The port to bind to
        channelProps.setProperty(TCPChannel.METRICS_INTERVAL_KEY, "10000"); //The interval to receive channel metrics
        channelProps.setProperty(TCPChannel.HEARTBEAT_INTERVAL_KEY, "1000"); //Heartbeats interval for established connections
        channelProps.setProperty(TCPChannel.HEARTBEAT_TOLERANCE_KEY, "3000"); //Time passed without heartbeats until closing a connection
        channelProps.setProperty(TCPChannel.CONNECT_TIMEOUT_KEY, "1000"); //TCP connect timeout
        channelId = createChannel(TCPChannel.NAME, channelProps); //Create the channel with the given properties
    }

    @Override
    public void join(String ip, int port) throws UnknownHostException {

    }

    @Override
    public String members() {
        return null;
    }

    @Override
    public void leave() {

    }

    @Override
    public void leave(String ip, int port) throws UnknownHostException {

    }

    @Override
    public Set<Host> neighbourhood() {
        return null;
    }

    @Override
    public void sendDirectMessage(String message, Host receiver) {

    }

    @Override
    public void sendGossip(int id, String message, Host receiver) {

    }

    @Override
    public void sendLeave(int id, Host receiver) {

    }
}
