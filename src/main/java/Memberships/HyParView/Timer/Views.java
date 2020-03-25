package Memberships.HyParView.Timer;

import babel.generic.ProtoTimer;

import javax.swing.text.View;

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
