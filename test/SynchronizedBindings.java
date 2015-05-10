import javax.script.Bindings;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * And390 - 09.05.2015
 */
public class SynchronizedBindings implements Bindings
{
    private final Bindings inner;
    public SynchronizedBindings (Bindings inner)  {  this.inner=inner;  }

    public synchronized Object get (Object _0)  {  return inner.get(_0);  }
    public synchronized Object put (String _0, Object _1)  {  return inner.put(_0,_1);  }
    public synchronized void putAll (Map<? extends String, ?> _0)  {  inner.putAll(_0);  }
    public synchronized Object remove (Object _0)  {  return inner.remove(_0);  }
    public synchronized boolean containsKey (Object _0)  {  return inner.containsKey(_0);  }
    public synchronized boolean equals (Object _0)  {  return inner.equals(_0);  }
    public synchronized Collection<Object> values ()  {  return inner.values();  }
    public synchronized int hashCode ()  {  return inner.hashCode();  }
    public synchronized void clear ()  {  inner.clear();  }
    public synchronized boolean isEmpty ()  {  return inner.isEmpty();  }
    public synchronized Set<Entry<String, Object>> entrySet ()  {  return inner.entrySet();  }
    public synchronized int size ()  {  return inner.size();  }
    public synchronized Set<String> keySet ()  {  return inner.keySet();  }
    public synchronized boolean containsValue (Object _0)  {  return inner.containsValue(_0);  }
}

