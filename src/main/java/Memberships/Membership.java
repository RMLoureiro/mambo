package Memberships;

import babel.generic.GenericProtocol;
import network.data.Host;


public abstract class Membership extends GenericProtocol {

    public Membership(String protoName, short protoId) {
        super(protoName, protoId);
    }

}
