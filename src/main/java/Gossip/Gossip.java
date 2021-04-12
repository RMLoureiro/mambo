package Gossip;

import Memberships.HyParView.HyParView;
import Memberships.Membership;
import babel.core.Babel;
import babel.exceptions.HandlerRegistrationException;
import babel.exceptions.InvalidParameterException;
import babel.exceptions.ProtocolAlreadyExistsException;
import network.data.Host;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

public class Gossip {

    Set<Host> neighbourhood;
    int fanout;

    public Membership membership;
    public Gossip(String[] args) throws IOException, InvalidParameterException, ProtocolAlreadyExistsException, HandlerRegistrationException, InterruptedException {
        Babel babel = Babel.getInstance();
        String[] arguments = Arrays.copyOfRange(args, 1, args.length);
        Properties configProps = babel.loadConfig(arguments, args[0]);

        Thread.sleep(1000);

        int type = Integer.parseInt(configProps.getProperty("membership"));
        fanout = Integer.parseInt(configProps.getProperty("active"));

        Membership membership = null;

        switch (type) {
            case 1:
                membership = new HyParView(this);
                break;

            case 2:
                //TODO
                break;

            default:
                System.out.println("LOGS-Invalid props configuration on membership type");
                System.exit(0);
        }


        membership.init(configProps);
        babel.registerProtocol(membership);
        babel.start();

    }

    public void newNode(Host node){ neighbourhood.add(node); }

    public void nodeDown(Host node){ neighbourhood.remove(node); }

    public void join(String ip, int port) throws UnknownHostException { membership.join(ip,port); }

    public void leave(String ip, int port) throws UnknownHostException { membership.leave(ip, port); }

    public void leave(String id){

    }

    public void leave(){ membership.leave(); }

    public String members(){
        return membership.members();
    }

    public void send(int id, String message){ }

    public void send(String message, String ip, int port) throws UnknownHostException {
        membership.sendDirectMessage(message, new Host(InetAddress.getByName(ip), port));
    }

    public void receive(int id, String message){}

    public void receive(String message){
        System.out.println("LOGS-MSG: " + message);
    }
}
