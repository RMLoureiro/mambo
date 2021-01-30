import Gossip.Gossip;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    /**
     Activate logger**/
    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }


    public static void main(String[] args) throws Exception {
        Gossip gossip = new Gossip(args);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        String line[] = reader.readLine().split(" ");
        String option = line[0];

        switch(option){
            case "JOIN":
                line = reader.readLine().split(":");
                gossip.join(line[0], Integer.parseInt(line[1]));
                break;
            case "LEAVE":
                break;
            case "MEMBERS":
                break;
            case "SEND":
                break;
            default:
                break;
        }
    }
}
