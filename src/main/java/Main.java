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
        Mambo mambo = new Mambo(args);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            String line[] = reader.readLine().split(" ");
            String option = line[0];
            switch(option){
                case "JOIN":
                    line = reader.readLine().split(":");
                    mambo.join(line[0], Integer.parseInt(line[1]));
                    break;
                case "LEAVE":
                    if(line.length == 1){
                        mambo.leave();
                    }else{
                        line = reader.readLine().split(":");
                        if(line.length == 2){
                            mambo.leave(line[0], Integer.parseInt(line[1]));
                        }else if(line.length == 1){
                            mambo.leave(line[0]);
                        }
                    }
                    break;
                case "MEMBERS":
                    String members = mambo.members();
                    System.out.println(members);
                    break;
                case "SEND":
                    line = reader.readLine().split(":");
                    if(line.length == 2){
                        mambo.send(reader.readLine(), line[0], Integer.parseInt(line[1]));
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
