package Memberships.Chord;

import babel.core.GenericProtocol;
import babel.exceptions.HandlerRegistrationException;

import java.io.IOException;
import java.util.Properties;

public class Chord extends GenericProtocol {

    public static short PROTOCOL_ID = 200;
    public static String PROTOCOL_NAME = "CHORD";

    public Chord() {
        super(PROTOCOL_NAME, PROTOCOL_ID);
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {

    }
}
