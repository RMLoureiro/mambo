import Memberships.HyParView.HyParView;
import babel.Babel;

import java.util.Properties;


public class Main {

    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    public static void main(String[] args) throws Exception {
        Babel babel = Babel.getInstance();
        Properties configProps = babel.loadConfig("network_config.properties", args);

        Thread.sleep(1000);

        HyParView membership = new HyParView();
        membership.init(configProps);
        babel.registerProtocol(membership);


        babel.start();
    }
}
