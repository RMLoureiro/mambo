import Gossip.Gossip;

public class Main {

    /**
     Activate logger**/
    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }


    public static void main(String[] args) throws Exception {
        Gossip gossip = new Gossip(args);
    }
}
