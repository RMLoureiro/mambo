package Gossip;

import babel.exceptions.HandlerRegistrationException;
import babel.exceptions.InvalidParameterException;
import babel.exceptions.ProtocolAlreadyExistsException;

import java.io.IOException;

public class EagerPush extends Gossip {
    public EagerPush(String[] args) throws IOException, InvalidParameterException, ProtocolAlreadyExistsException, HandlerRegistrationException, InterruptedException {
        super(args);
    }


}
