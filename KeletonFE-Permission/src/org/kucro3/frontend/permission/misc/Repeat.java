package org.kucro3.frontend.permission.misc;

import java.util.Arrays;

public final class Repeat {
    public static Repeat single(char character)
    {
        return repeat(1, character);
    }

    public static Repeat empty(char character)
    {
        return repeat(0, character);
    }

    public static Repeat repeat(int time, char character)
    {
        return new Repeat(time, character);
    }

    private Repeat(int time, char character)
    {
        this.length = time;
        this.character = character;

        char[] array = new char[time];
        Arrays.fill(array, character);
        this.string = new String(array);
    }

    public int length()
    {
        return length;
    }

    public char getCharacter()
    {
        return character;
    }

    public Repeat repeat(int time)
    {
        return new Repeat(time, character);
    }

    public Repeat increase()
    {
        return repeat(length + 1);
    }

    public Repeat decrease()
    {
        return repeat(length - 1);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
            return false;
        if(obj instanceof String)
            return string.equals(obj);
        if(obj instanceof Repeat)
            return string.equals(((Repeat) obj).string);
        return true;
    }

    @Override
    public int hashCode()
    {
        return string.hashCode();
    }

    private final String string;

    private final char character;

    private final int length;
}
