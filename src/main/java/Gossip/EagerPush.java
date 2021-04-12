package Gossip;

import babel.exceptions.HandlerRegistrationException;
import babel.exceptions.InvalidParameterException;
import babel.exceptions.ProtocolAlreadyExistsException;
import network.data.Host;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class EagerPush extends Gossip {
    Set<Integer> msgs;
    public EagerPush(String[] args) throws IOException, InvalidParameterException, ProtocolAlreadyExistsException, HandlerRegistrationException, InterruptedException {
        super(args);
        msgs = new HashSet<>();
    }

    public void send(int id, String message){
        Set<Host> neighbours = membership.neighbourhood();
        Random rnd = new Random();
        for(int i = 0; i < fanout; i++){
            Host neigh = (Host) neighbours.toArray()[rnd.nextInt(neighbours.size())];
            neighbours.remove(neigh);
            membership.sendGossip(id, message, neigh);
        }
    }

    public void receive(int id, String message){
        if(!msgs.contains(id)){
            msgs.add(id);
            System.out.println("LOGS-MSG: " + message);
            send(id, message);
        }
    }
}
