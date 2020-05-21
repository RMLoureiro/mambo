package Memberships.HyParView.Timers;

import babel.generic.ProtoTimer;

public class ShuffleT extends ProtoTimer {

    public static final short TIMER_CODE = 102;

    public ShuffleT() {
        super(ShuffleT.TIMER_CODE);
    }

    @Override
    public ProtoTimer clone() {
        return this;
    }
}
