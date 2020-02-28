import Memberships.Membership;
import babel.Babel;
import babel.exceptions.HandlerRegistrationException;
import babel.exceptions.InvalidParameterException;
import babel.exceptions.ProtocolAlreadyExistsException;

import java.io.IOException;
import java.util.Properties;


public class Main {

    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    public static void main(String[] args) throws Exception {
        Babel babel = Babel.getInstance();
        Properties configProps = babel.loadConfig("network_config.properties", args);

        Thread.sleep(1000);

        Membership membership = new Membership();
        membership.init(configProps);
        babel.registerProtocol(membership);


        babel.start();
    }
}
