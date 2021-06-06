package Memberships.Kademlia;

import network.data.Host;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;

public class RoutingTable {

    public Bucket root;

    public RoutingTable(){
        root = new Bucket("");
    }

    public Bucket getBucket(String id){
        Bucket temp = root;
        for(int i = 0; i < id.length(); i++){
            if(id.charAt(i) == '1'){
               if(temp.getRight() == null){
                   return temp;
               }else{
                   temp = temp.getRight();
               }
            }else if(id.charAt(i) == '0'){
                if(temp.getLeft() == null){
                    return temp;
                }else{
                    temp = temp.getLeft();
                }
            }else{
                return null;
            }
        }
        return null;
    }

    public void splitBucket(Bucket bucket){
        String id = bucket.getPrefix();
        bucket.setLeft(new Bucket(id + '0'));
        bucket.setRight(new Bucket(id + '1'));

        Iterator<Pair<String, Host>> list = bucket.getList().iterator();

        while(list.hasNext()){
            Pair<String, Host > next = list.next();
            if(next.getKey().charAt(id.length()) == '0'){
                bucket.getRight().add(next.getKey(), next.getValue());
            }else if(next.getKey().charAt(id.length()) == '1'){
                bucket.getLeft().add(next.getKey(), next.getValue());
            }
        }
    }
}
