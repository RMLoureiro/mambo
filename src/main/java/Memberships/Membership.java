package Memberships;

import babel.core.GenericProtocol;
import babel.exceptions.HandlerRegistrationException;
import babel.exceptions.InvalidParameterException;
import babel.exceptions.ProtocolAlreadyExistsException;
import network.data.Host;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Set;


public abstract class Membership extends GenericProtocol {

    public Membership(String protoName, short protoId) {
        super(protoName, protoId);
    }

    public abstract void join(String ip, int port) throws UnknownHostException;

    public abstract String members();

    public abstract void leave();

    public abstract void leave(String ip, int port) throws UnknownHostException;

    public abstract Set<Host> neighbourhood();

    public abstract void sendDirectMessage(String message, Host receiver);

    public abstract void sendGossip(int id, String message, Host receiver);

    public abstract void sendLeave(int id, Host receiver);
}
