package org.kucro3.frontend.permission.misc;

public class Increment {
    public Increment()
    {
    }

    public Increment increase()
    {
        value++;
        return this;
    }

    public int value()
    {
        return value;
    }

    private int value;
}
