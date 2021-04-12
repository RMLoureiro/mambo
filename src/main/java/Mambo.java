import Gossip.Gossip;
import Gossip.EagerPush;
import Memberships.HyParView.HyParView;
import Memberships.Membership;
import babel.core.Babel;
import babel.exceptions.HandlerRegistrationException;
import babel.exceptions.InvalidParameterException;
import babel.exceptions.ProtocolAlreadyExistsException;
import network.data.Host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

public class Mambo {

    Gossip gossip;
    public Mambo(String[] args) throws InterruptedException, ProtocolAlreadyExistsException, InvalidParameterException, HandlerRegistrationException, IOException {
        Babel babel = Babel.getInstance();
        String[] arguments = Arrays.copyOfRange(args, 1, args.length);
        Properties configProps = babel.loadConfig(arguments, args[0]);

        int type = Integer.parseInt(configProps.getProperty("gossip"));
        switch (type) {
            case 1:
                gossip = new EagerPush(args);
                break;

            case 2:
                //TODO
                break;

            default:
                System.out.println("LOGS-Invalid props configuration on gossip type");
                System.exit(0);
        }
    }

    public void join(String ip, int port) throws UnknownHostException { gossip.join(ip,port); }

    public void leave(String ip, int port) throws UnknownHostException { gossip.leave(ip, port); }

    public void leave(String id){

    }

    public void leave(){ gossip.leave(); }

    public String members(){
        return gossip.members();
    }

    public void send(String message){
        gossip.send(message.hashCode(), message);
    }

    public void send(String message, String ip, int port) throws UnknownHostException {
        gossip.send(message, ip, port);
    }
}
