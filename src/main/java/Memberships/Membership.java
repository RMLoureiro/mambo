package Memberships;

import babel.core.GenericProtocol;
import network.data.Host;

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
}
