package ru.and390.utils;

import java.util.Iterator;

/**
 * User: And390
 * Date: 06.12.13
 * Time: 23:47
 */
public class ArrayIterable<T> implements Iterable<T>
{
    public T[] array;
    public ArrayIterable(T[] array)  {  this.array = array;  }

    @Override
    public Iterator<T> iterator()
    {
        return new ArrayIterator<T> (array);
    }
}
