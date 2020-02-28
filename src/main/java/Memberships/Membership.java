package Memberships;

import Memberships.HyParView.HyParView;
import babel.exceptions.HandlerRegistrationException;
import babel.generic.GenericProtocol;
import network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class Membership extends GenericProtocol {

    public static short PROTOCOL_ID;
    public static String PROTOCOL_NAME;

    Host myself;

    Membership membership;

    public Membership() {
        super(PROTOCOL_NAME, PROTOCOL_ID);
    }

    @Override
    public void init(Properties props) throws HandlerRegistrationException, IOException {
        PROTOCOL_ID = Short.parseShort(props.getProperty("membership"));
        switch(PROTOCOL_ID){
            case 1:
                PROTOCOL_NAME = "HyParView";
                 membership = new HyParView(props);
                 myself = membership.myself;
                break;
        }
    }
}
