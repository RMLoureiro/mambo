package Memberships.HyParView.Timer;

import babel.generic.ProtoTimer;

public class CheckAcks extends ProtoTimer {

    public static final short TIMER_CODE = 103;

    public CheckAcks() {
        super(CheckAcks.TIMER_CODE);
    }

    @Override
    public ProtoTimer clone() {
        return this;
    }
}
