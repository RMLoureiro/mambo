import Memberships.HyParView.HyParView;
import babel.Babel;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Properties;


public class Main {

    /**
     Activate logger**/
    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }


    public static void main(String[] args) throws Exception {
        Babel babel = Babel.getInstance();
        String[] arguments = Arrays.copyOfRange(args, 1, args.length);
        Properties configProps = babel.loadConfig(args[0], arguments);

        Thread.sleep(1000);

        HyParView membership = new HyParView();
        membership.init(configProps);
        babel.registerProtocol(membership);


        babel.start();
    }
}
