import Gossip.Gossip;
import Memberships.HyParView.HyParView;
import Memberships.Membership;
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
        Gossip gossip = new Gossip(args);
        /**MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("string".getBytes());
        BigInteger bi = new BigInteger(1, hash);**/
    }
}
