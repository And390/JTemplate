package ru.and390.utils;

import java.util.ArrayList;

/**
 * Since Java 7 it is not more effective than StringBuilder
 * And390 - 25.12.13
 */
public class StringList extends ArrayList<String> implements RuntimeAppendable
{
    public StringList()  {}
    public StringList(String string)  {  add(string);  }

    // throw IndexOutOfBoundsException if newSize>size
    public void setSize(int newSize)
    {
        removeRange(newSize, size());
    }

    public StringList append(String string)  {
        add(string);
        return this;
    }

    public StringList append(Object object)  {
        add(object.toString());
        return this;
    }

    @Override
    public StringList append(CharSequence csq)  {
        add(csq.toString());
        return this;
    }

    @Override
    public StringList append(CharSequence csq, int start, int end)  {
        add(csq.subSequence(start, end).toString());
        return this;
    }

    @Override
    public StringList append(char c)  {
        add(""+c);
        return this;
    }

    public StringList append(char[] chars, int start, int len)  {
        add(new String (chars, start, len));
        return this;
    }

    public StringList append(StringList array)  {
        this.addAll(array);
        return this;
    }

    public StringList appendSep(String separator, String string)  {
        if (size()!=0)  add(separator);
        add(string);
        return this;
    }

    public StringList attach(String value)  {
        this.add(0, value);
        return this;
    }

    @Override
    public String toString()
    {
        return Util.toString(this);
    }
}
