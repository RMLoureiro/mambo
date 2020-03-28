package Memberships.HyParView.Timer;

import babel.generic.ProtoTimer;

import javax.swing.text.View;

public class ShuffleT extends ProtoTimer {

    public static final short TIMER_CODE = 102;

    public ShuffleT() {
        super(Views.TIMER_CODE);
    }

    @Override
    public ProtoTimer clone() {
        return this;
    }
}
