package Memberships.HyParView;

import Memberships.Membership;
import network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class HyParView extends Membership {

    Host myself;
    int channelId;

    public HyParView(Properties props){
        try {
            myself = new Host(InetAddress.getByName(props.getProperty("address")),
                    Integer.parseInt(props.getProperty("port")));
            channelId = createChannel("Ackos", props);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }
    }
}
