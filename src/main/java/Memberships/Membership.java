package Memberships;

import babel.core.GenericProtocol;
import network.data.Host;

import java.net.UnknownHostException;


public abstract class Membership extends GenericProtocol {

    public Membership(String protoName, short protoId) {
        super(protoName, protoId);
    }

    public abstract void join(String ip, int port) throws UnknownHostException;

    public abstract String members();
}
