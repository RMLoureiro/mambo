package Memberships.Kademlia;

public class RoutingTable {

    public Bucket bucket;
    public RoutingTable left, right;
    public String prefix;

    public RoutingTable(){
        left = null;
        right = null;
        bucket = new Bucket();
        prefix = "";
    }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public String getPrefix(){
        return prefix;
    }

    public void setRight(RoutingTable right){
        this.right = right;
    }

    public RoutingTable getRight(){
        return right;
    }

    public void setLeft(RoutingTable left){
        this.left = left;
    }

    public RoutingTable getLeft(){
        return left;
    }
}
