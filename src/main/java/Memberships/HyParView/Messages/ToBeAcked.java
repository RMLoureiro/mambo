package Memberships.HyParView.Messages;

import network.data.Host;

public class ToBeAcked {
    Host host;
    int hash, code;

    public ToBeAcked(Host host, int hash, int code){
        this.host = host;
        this.hash = hash;
        this.code = code;
    }

    public Host getHost() {
        return host;
    }

    public int getHash() {
        return hash;
    }

    public int getCode() {
        return code;
    }
}
