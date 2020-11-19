package Memberships;

import babel.core.GenericProtocol;
import network.data.Host;


public abstract class Membership extends GenericProtocol {

    public Membership(String protoName, short protoId) {
        super(protoName, protoId);
    }

}
