package Memberships.Kademlia;

import network.data.Host;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Bucket {

    List<Pair<String, Host>> list;
    public Bucket left, right;
    public String prefix;

    public Bucket(String prefix){
        list = new ArrayList<>();
        left = null;
        right = null;
        this.prefix = prefix;
    }

    public Iterator<Pair<String, Host>> filter(String prefix){
        List<Pair<String, Host>> filtered = new ArrayList<>();
        for(Pair<String, Host> element : list){
            if(!element.getKey().startsWith(prefix)) {
                list.remove(element);
                filtered.add(element);
            }
        }
        return filtered.iterator();
    }


    public void add(String id, Host host){
        list.add(new ImmutablePair<>(id, host));
    }

    public int size(){
        return list.size();
    }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public String getPrefix(){
        return prefix;
    }

    public void setRight(Bucket right){
        this.right = right;
    }

    public Bucket getRight(){
        return right;
    }

    public void setLeft(Bucket left){
        this.left = left;
    }

    public Bucket getLeft(){
        return left;
    }

    public List<Pair<String, Host>> getList() { return list;}
}
