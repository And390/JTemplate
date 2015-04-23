package ru.and390.utils;

/**
 * And390 - 26.09.14
 */
public interface RuntimeAppendable extends Appendable
{
    @Override
    public RuntimeAppendable append(CharSequence csq);

    @Override
    public RuntimeAppendable append(CharSequence csq, int start, int end);

    @Override
    public RuntimeAppendable append(char c);

    /*
    public static RuntimeAppendable adapt(final Appendable appendable)
    {
        return new RuntimeAppendable()
        {
            public RuntimeAppendable append(CharSequence csq) {
                try  {  appendable.append(csq);  return this;  }
                catch (RuntimeException e)  {  throw e;  }
                catch (Exception e)  {  throw new RuntimeException (e);  }
            }

            public RuntimeAppendable append(CharSequence csq, int start, int end) {
                try  {  appendable.append(csq, start, end);  return this;  }
                catch (RuntimeException e)  {  throw e;  }
                catch (Exception e)  {  throw new RuntimeException (e);  }
            }

            @Override
            public RuntimeAppendable append(char c) {
                try  {  appendable.append(c);  return this;  }
                catch (RuntimeException e)  {  throw e;  }
                catch (Exception e)  {  throw new RuntimeException (e);  }
            }
        };
    }
    */
}
