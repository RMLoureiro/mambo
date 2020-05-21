package Memberships.HyParView.Timers;

import babel.generic.ProtoTimer;

public class Views extends ProtoTimer {

    public static final short TIMER_CODE = 101;

    public Views() {
        super(Views.TIMER_CODE);
    }

    @Override
    public ProtoTimer clone() {
        return this;
    }
}
