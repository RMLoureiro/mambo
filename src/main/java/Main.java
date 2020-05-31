import Memberships.HyParView.HyParView;
import Memberships.Membership;
import babel.Babel;

import java.math.BigInteger;

import java.security.MessageDigest;
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

        int memb = Integer.parseInt(configProps.getProperty("membership"));
        Membership membership = null;

        switch (memb) {
            case 1:
                membership = new HyParView();
                break;

            case 2:
                //TODO
                break;

            default:
                System.out.println("LOGS-Invalid props configuration on membership type");
                System.exit(0);
        }


        membership.init(configProps);
        babel.registerProtocol(membership);
        babel.start();

        /**MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("string".getBytes());
        BigInteger bi = new BigInteger(1, hash);**/
    }
}
