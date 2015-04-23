package ru.and390.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * User: And390
 * Date: 06.12.13
 * Time: 23:44
 */
public class ArrayIterator<T> implements Iterator<T>
{
    public T[] array;
    public int index = 0;
    public ArrayIterator(T[] array)  {  this.array = array;  }

    public boolean hasNext()
    {
        return index!=array.length;
    }

    public T next()
    {
        if (index==array.length) throw new NoSuchElementException ();
        return array[index++];
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
