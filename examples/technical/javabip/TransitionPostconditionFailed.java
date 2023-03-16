package vct.examples.technical.javabip;

import org.javabip.annotations.*;
import org.javabip.api.PortType;

@ComponentType(initial = INIT, name = NAME)
@Port(name = GO, type = PortType.enforceable)
@Invariant("x >= 0")
public class TransitionPostconditionFailed {
    public static final String INIT = "initialState";
    public static final String DONE = "doneState";
    public static final String NAME = "oneComponentOneTransition";
    public static final String GO = "go";

    OneComponentOneTransition() {
        x = 0;
    }

    private int x;

    @Transition(name = GO, source = INIT, target = DONE, requires = "x >= 0", ensures = "x < 3")
    public void goTransition() {
        x = 4;
    }
}

