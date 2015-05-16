package ru.and390.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Consumer - последовательно обрабатывает какие-то объекты. Интерфес общего назначения.
 * User: And390
 * Date: 20.12.14
 * Time: 1:09
 */
public interface Consumer<T, ET extends Throwable>
{
    public void process(T object) throws ET;

    public interface E<T> extends Consumer<T, Exception>  {}
    public interface R<T> extends Consumer<T, RuntimeException>  {}
    public interface IO<T> extends Consumer<T, IOException>  {}


    public static abstract class Instances
    {
        public static <T> Consumer<T, RuntimeException> get(final Collection<T> collection)  {
            return new Consumer<T, RuntimeException> ()  {
                public void process(T object)  {  collection.add(object); }
            };
        }
    }

    public static class ToList<T> implements Consumer<T, RuntimeException>
    {
        public ArrayList<T> result = new ArrayList<T> ();
        public void process(T object)  {  result.add(object);  }
    }

    public static class Counter<T> implements Consumer<T, RuntimeException>
    {
        public int count;
        public void process(T object)  {  count++;  }
    }
}
