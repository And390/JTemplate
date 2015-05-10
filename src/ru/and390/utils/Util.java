package ru.and390.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * User: And390
 * Date: 26.04.14
 * Time: 20:20
 */
@SuppressWarnings("unused")
public class Util
{

    //----------------        system        ----------------

    public static boolean isWindows()  {  return System.getProperty("os.name").contains("Windows");  }

	public static boolean isMac()  {  return System.getProperty("os.name").contains("Mac OS");  }

	public static boolean isLinux()  {  return System.getProperty("os.name").contains("Linux");  }


    //----------------        strings        ----------------

    // в отличие от стандартного indexOf возвращает длину строки, если символ не найден
    public static int indexOf(String string, char target, int offset)  {
        int result = string.indexOf(target, offset);
        return result==-1 ? string.length() : result;
    }

    public static int indexOf(String string, String target, int offset)  {
        int result = string.indexOf(target, offset);
        return result==-1 ? string.length() : result;
    }

    public static int indexOfWord(String source, String target, int offset)  {
        for (int i2=offset;;)  {
            int i1 = source.indexOf(target, i2);
            if (i1==-1)  return -1;
            i2 = i1 + target.length();
            if ((i1==0 || source.charAt(i1-1)<=' ') && (i2==source.length() || source.charAt(i2)<=' '))  return i1;
        }
    }
    public static int indexOfWord(String source, String target)  {  return indexOfWord(source, target, 0);  }

    // считает количество указанных символов в строке
    public static int count(String string, char target)
    {
        int n=0;
        for (int i=0; i!=string.length(); i++)  {
            i = string.indexOf(target, i);
            if (i==-1)  break;
            n++;
        }
        return n;
    }

    // считает количество подстрок в строке
    public static int count(String string, String target)
    {
        int n=0;
        for (int i=0; i!=string.length(); i+=target.length())  {
            i = string.indexOf(target, i);
            if (i==-1)  break;
            n++;
        }
        return n;
    }

    //    slice
    //разбивает строку по заданному символу-разделителю;
    // если символ не встречается, то результатом будет целая строка, как частный вариант - пустая;
    // если разделители идут подряд, между ними будут пустые части, аналогично, если разделитель стоит в конце;
    //аналог стандартного split, но тот игнорирует разделители в конце

    public static <T extends Throwable> void slice(String string, char separator, Consumer<String, T> handler) throws T
    {
        for (int i=0; ; i++)  {
            int i0=i;
            i = indexOf(string, separator, i);
            handler.process(string.substring(i0, i));
            if (i==string.length())  break;
        }
    }

    public static String[] slice(String string, char separator)
    {
        //    посчитать количество
        int count = count(string, separator) + 1;
        //    установить значения и вернуть
        final String[] result = new String [count];
        slice(string, separator, new Consumer.R<String> ()  {
            int i = 0;
            public void process(String string)  {  result[i++] = string;  }
        });
        return result;
    }

    public static <T extends Throwable> void slice(String string, String separator, Consumer<String, T> consumer) throws T
    {
        for (int i=0; ; i+=separator.length())  {
            int i0=i;
            i = indexOf(string, separator, i);
            consumer.process(string.substring(i0, i));
            if (i==string.length())  break;
        }
    }

    public static String[] slice(String string, String separator)
    {
        //    посчитать количество
        int count = count(string, separator) + 1;
        //    установить значения и вернуть
        final String[] result = new String [count];
        slice(string, separator, new Consumer.R<String> ()  {
            int i = 0;
            public void process(String string)  {  result[i++] = string;  }
        });
        return result;
    }

    public static <T extends Collection<String>> T slice(String string, String separator, final T result)
    {
        slice(string, separator, new Consumer.R<String> ()  {
            public void process(String string)  {  result.add(string);  }
        });
        return result;
    }

    public static String[] slice(String string, String separator, boolean ignoreLast)  {
        if (ignoreLast)  string = cutIfEnds(string, separator);
        return slice(string, separator);
    }

    public static class TestSlice  {
        public static void main(String[] args)  {
            if (!Arrays.equals(slice("", '\t'), new String[] { "" }))  throw new RuntimeException ("not match");
            if (!Arrays.equals(slice("\t", '\t'), new String [] { "", "" }))  throw new RuntimeException ("not match");
            if (!Arrays.equals(slice("abc", '\t'), new String [] { "abc" }))  throw new RuntimeException ("not match");
            if (!Arrays.equals(slice("\tabc", '\t'), new String [] { "", "abc"}))  throw new RuntimeException ("not match");
            if (!Arrays.equals(slice("abc\t", '\t'), new String [] { "abc", "" }))  throw new RuntimeException ("not match");
            if (!Arrays.equals(slice("\tabc\t", '\t'), new String [] { "", "abc", "" }))  throw new RuntimeException ("not match");
            if (!Arrays.equals(slice("x\tyyy", '\t'), new String [] { "x", "yyy" }))  throw new RuntimeException ("not match");
            if (!Arrays.equals(slice("x\t\tyyy", '\t'), new String [] { "x", "", "yyy" }))  throw new RuntimeException ("not match");
            if (!Arrays.equals(slice("\tx\t\tyyy\tz\t", '\t'), new String [] { "", "x", "", "yyy", "z", "" }))  throw new RuntimeException ("not match");
        }
    }

    //    sliceWords делит по пробельным символам; слова всегда не пустые

    public static <T extends Throwable> void sliceWords(String string, Consumer<String, T> consumer) throws T
    {
        for (int i=0; ; )  {
            for (;;)  {  if (i==string.length())  return;  if (string.charAt(i)>' ')  break;  i++;  }
            int i0=i;
            for (;;)  {  i++;  if (i==string.length())  break;  if (string.charAt(i)<=' ')  break;  }
            consumer.process (string.substring(i0, i));
        }
    }

    public static String[] sliceWords(String string)
    {
        //    посчитать количество
        Consumer.Counter<String> counter = new Consumer.Counter<> ();
        sliceWords(string, counter);
        //    установить значения и вернуть
        final String[] result = new String [counter.count];
        sliceWords(string, new Consumer.R<String> ()  {
            int i = 0;
            public void process(String string)  {  result[i++] = string;  }
        });
        return result;
    }

    public static class TestSliceWords  {
        public static void main(String[] args)  {
            if (!Arrays.equals(sliceWords(""), new String [] {}))  throw new RuntimeException ("not match");
            if (!Arrays.equals(sliceWords("  "), new String [] {}))  throw new RuntimeException ("not match");
            if (!Arrays.equals(sliceWords("abc"), new String [] {"abc"}))  throw new RuntimeException ("not match");
            if (!Arrays.equals(sliceWords(" abc"), new String [] {"abc"}))  throw new RuntimeException ("not match");
            if (!Arrays.equals(sliceWords("abc "), new String [] {"abc"}))  throw new RuntimeException ("not match");
            if (!Arrays.equals(sliceWords("   abc  "), new String [] {"abc"}))  throw new RuntimeException ("not match");
            if (!Arrays.equals(sliceWords("x yyy"), new String [] {"x", "yyy"}))  throw new RuntimeException ("not match");
            if (!Arrays.equals(sliceWords(" x  yyy   z "), new String [] {"x", "yyy", "z"}))  throw new RuntimeException ("not match");
        }
    }

    //    sliceRows
    //делит по разделителям строк, корректно обрабатывает разделители строк '\r', '\n' и '\r\n\'

    public static <T extends Throwable> void sliceRows(String string, Consumer<String, T> consumer) throws T
    {
        if (string.length()==0)  {
            consumer.process(string);
            return;
        }
        for (int in=-1, ir=-1; ; )  {
            int i0=ir+1;  //начало новой строки, будет установлено в Min(ir+1, in+1) или в in+1 для случая '\r\n'
            if (ir<=in)  {  ir = indexOf(string, '\r', ir+1);  }            //для ir<in и на первой итерации (ir=in) надо передвинуть ir
            if (in<=i0)  {  i0=in+1;  in = indexOf(string, '\n', in+1);  }  //для in<ir, in=ir+1 (случай '\r\n') и первой итерации надо передвинуть in
            consumer.process(string.substring(i0, ir < in ? ir : in));
            if (ir==string.length() && in==string.length())  break;
        }
    }

    public static <T extends Throwable> void sliceRows(String string, boolean ignoreLast, Consumer<String, T> consumer) throws T
    {
        if (ignoreLast)  string = cutLastNL(string);
        sliceRows(string, consumer);
    }

    public static String[] sliceRows(String string)
    {
        //    calculate count
        Consumer.Counter<String> counter = new Consumer.Counter<> ();
        sliceRows(string, counter);
        //    slice values and return
        final String[] result = new String [counter.count];
        sliceRows(string, new Consumer.R<String> ()  {
            int i=0;
            public void process(String string)  {  result[i++] = string;  }
        });
        return result;
    }

    public static String[] sliceRows(String string, boolean ignoreLast)
    {
        if (ignoreLast)  string = cutLastNL(string);
        return sliceRows(string);
    }


    //    аналог javascript join

    public static String toString(Iterable<String> strings, String separator)
    {
        //    посчитать длину целиком и количество элементов, проверить null-значения
        int len = 0;
        int size = 0;
        for (String string : strings)
            if (string!=null)  {  len += string.length() + separator.length();  size++;  }
            else  throw new NullPointerException ("toString is not support nulls");
        //    если элементов ноль или один, эффективней сразу вернуть результат
        if (size==0)  return "";
        if (size==1)  return strings.iterator().next();
        len -= separator.length();  //теперь можно удалить последний разделитель
        //    выделить буфер и скопировать туда байты
        char[] buffer = new char [len];
        int destOffset = 0;
        for (String string : strings)  {
            string.getChars(0, string.length(), buffer, destOffset);
            destOffset += string.length();
            if (separator.length()!=0 && destOffset!=len)  {
                separator.getChars(0, separator.length(), buffer, destOffset);
                destOffset += separator.length();
            }
        }
        return new String (buffer);
    }

    public static String toString(Iterable<String> strings)  {
        return toString(strings, "");
    }

    public static String toString(String[] strings, String separator)  {
        return toString(new ArrayIterable<>(strings), separator);
    }

    public static String toString(String[] strings)  {
        return toString(new ArrayIterable<>(strings), "");
    }

    public static class TestToString  {
        public static void main(String[] args)  {
            System.out.println(Util.toString(new String[]{}));
            System.out.println(Util.toString(new String[]{"xxx"}));
            System.out.println(Util.toString(new String[]{"abc", "", "def"}));
        }
    }


    //    прочие вспомогательные функции со строками

    public static String notNull(String string)  {  return string!=null ? string : "";  }

    public static boolean isEmpty(String value)  {  if (value==null)  return true;  return value.length()==0;  }
    public static boolean isNotEmpty(String value)  {  if (value==null)  return false;  return value.length()!=0;  }

    public static boolean isTrimEmpty(String value)  {  if (value==null)  return true;  return value.trim().length()==0;  }
    public static boolean isNotTrimEmpty(String value)  {  if (value==null)  return false;  return value.trim().length()!=0;  }

    public static String notEmpty(String value1, String value2)  {
        if (value1!=null)  if (value1.length()!=0)  return value1;
        return value2;
    }
    public static String notEmpty(String value1, String value2, String value3)  {
        if (value1!=null)  if (value1.length()!=0)  return value1;
        if (value2!=null)  if (value2.length()!=0)  return value2;
        return value3;
    }

    public static String cut(String string, int count)  {
        return string.substring(0, string.length()-count);
    }

    public static String cutIfStarts(String string, String prefix)  {
        return string.startsWith(prefix) ? string.substring(prefix.length()) : string;
    }

    public static String cutIfEnds(String string, String suffix)  {
        return string.endsWith(suffix) ? string.substring(0, string.length()-suffix.length()) : string;
    }

    public static String cutAfter(String string, char target)  {
        int i = string.indexOf(target);
        return i==-1 ? string : string.substring(0, i);
    }

    public static String cutAfter(String string, String target)  {
        int i = string.indexOf(target);
        return i==-1 ? string : string.substring(0, i);
    }

    public static String cutBefore(String string, char target)  {
        int i = string.lastIndexOf(target);
        return i==-1 ? string : string.substring(i+1);
    }

    public static String cutBefore(String string, String target)  {
        int i = string.lastIndexOf(target);
        return i==-1 ? string : string.substring(i+target.length());
    }

    public static String cutLastNL(String string)  {
        if (string.endsWith("\r\n"))  string = Util.cut(string, 2);
        else if (string.endsWith("\n"))  string = Util.cut(string, 1);
        else if (string.endsWith("\r"))  string = Util.cut(string, 1);
        return string;
    }

    public static String cutLastLine(String string)  {
        int i = string.lastIndexOf('\n');
        int i2 = string.indexOf('\r', i+1);
        if (i2!=-1)  i = i2;
        else if (i>0 && string.charAt(i-1)=='\r')  i--;
        return i==-1 ? string : string.substring(0, i);
    }

    public static String ellipsis(String source, int n)  {
        return source.length()>n ? source.substring(0, n-3)+"..." : source;
    }

    public static String numEnding(int num, String nominativ, String genetiv, String plural)  {
        num = Math.abs(num);
        if (num%10==0 || num%10>=5 || num%100 >= 11 && num%100 <= 14)  return plural;
        else if (num%10==1)  return nominativ;
        else  return genetiv;
    }

    public static String num(int num, String nominativ, String genetiv, String plural)  {
        return num + numEnding(num, nominativ, genetiv, plural);
    }

    public static String quote(String string)  {
        return quote(string, new StringList()).toString();
    }
    public static RuntimeAppendable quote(String string, RuntimeAppendable result)  {
        return escape(string, result.append('"'), new char[]{'\\', '"', '\t', '\r', '\n'},
                new String[]{"\\\\", "\\\"", "\\t", "\\r", "\\n"}).append('"');
    }

    public static String escape(String string)  {
        return escape(string, new StringList ()).toString();
    }
    public static RuntimeAppendable escape(String string, RuntimeAppendable result)  {
        return escape(string, result, new char[] { '\\', '\t', '\r', '\n' }, new String[] { "\\\\", "\\t", "\\r", "\\n" });
    }

    public static String escape(String string, char... chars)  {
        return escape(string, new StringList (), chars).toString();
    }
    public static RuntimeAppendable escape(String string, RuntimeAppendable result, char... chars)  {
        chars = Arrays.copyOf(chars, chars.length+1);
        chars[chars.length-1] = '\\';
        return escape(string, result, "\\", chars);
    }
    public static RuntimeAppendable escape(String string, RuntimeAppendable result, String escapeChar, char... chars)  {
        String[] replacers = new String [chars.length];
        for (int i=0; i<chars.length; i++)  replacers[i] = escapeChar+chars[i];
        return escape(string, result, chars, replacers);
    }

    public static RuntimeAppendable escape(String string, RuntimeAppendable result, char[] targets, String[] replacers)  {
        escape(string, result, targets, replacers, 0);
        return result;
    }
    private static void escape(String string, RuntimeAppendable result, char[] targets, String[] replacers, int index)  {
        if (index==targets.length)  {
            result.append(string);
            return;
        }
        for (int i=0;; i++)  {
            //    find next occurence of char that need to be escaped
            int i0 = i;
            i = string.indexOf(targets[index], i);
            if (i==-1)  {
                //    if end, add last part and exit
                if (i0!=0)  string = string.substring(i0);
                escape(string, result, targets, replacers, index+1);
                break;
            }
            //    add part before finded char
            if (i0!=i)  {
                if (i0+1==i)  {
                    //    escape rest specail chars in this one-char part
                    char c = string.charAt(i0);
                    for (int t=index; ; t++)
                        if (t==targets.length)  {  result.append(c);  break;  }
                        else if (c==targets[t])  {  result.append(replacers[t]);  break;  }
                }
                else  {
                    //    recursively escape rest specail chars in this part
                    String part = string.substring(i0, i);
                    escape(part, result, targets, replacers, index+1);
                }
            }
            //    add escaped char
            result.append(replacers[index]);
        }
    }

    private static class Test  {
        public static void check(String source, String expect) throws Exception  {
            String result = escape(source);
            System.out.println(result);
            if (!result.equals(expect))  throw new Exception ("is not equal to: "+expect);
        }
        public static void main(String[] args) throws Exception  {
            check("", "");
            check(" abc\f", " abc\f");
            check("\\", "\\\\");
            check("a\tb", "a\\tb");
            check("xxx\rx\nyyy\\zzz", "xxx\\rx\\nyyy\\\\zzz");
        }
    }

    public static String unescape(String string)  {
        int n = count(string, '\\') - count(string, "\\\\");
        if (n!=0)  for (int i=string.length()-1, c=-1; i>=0 && string.charAt(i)=='\\'; i--, c=-c)  n+=c;
        if (n==0)  return string;
        final char[] result = new char [string.length()-n];
        slice(string, '\\', new Consumer.R<String> ()
        {
            int offset = 0;
            boolean last = false;
            public void process(String string)
            {
                if (string.length()==0)  {
                    if (last)  result[offset++] = '\\';
                    last = !last;
                }
                else  {
                    string.getChars(0, string.length(), result, offset);
                    if (last)  {
                        if (result[offset]=='n')  result[offset]='\n';
                        else if (result[offset]=='r')  result[offset]='\r';
                        else if (result[offset]=='t')  result[offset]='\t';
                    }
                    offset += string.length();
                    last = true;
                }

            }
        });
        return new String (result);
    }

    public static class TestUnescape
    {
        public static void check(String source, String expect) throws Exception  {
            String result = unescape(source);
            System.out.println(result);
            if (!result.equals(expect))  throw new Exception ("is not equal to: "+expect);
        }
        public static void main(String[] args) throws Exception  {
            check("", "");
            check("a", "a");
            check("\\", "\\");
            check("\\\\", "\\");
            check("\\\\\\", "\\\\");
            check("\\\\\\\\", "\\\\");
            check("\\xxx", "xxx");
            check("xxx\\", "xxx\\");
            check("\\xxx\\", "xxx\\");
            check("\\\\xxx\\", "\\xxx\\");
            check("\\xxx\\\\", "xxx\\");
            check("xxx\\\\\\\\", "xxx\\\\");
            check("xxx\\\\\\\\\\\\", "xxx\\\\\\");
            check("xxx\\xxx", "xxxxxx");
            check("xxx\\\\xxx", "xxx\\xxx");
            check("xxx\\\\\\xxx", "xxx\\xxx");
            check("xxx\\\\\\\\xxx", "xxx\\\\xxx");
            check("\\xxx\\\\\\xxx\\", "xxx\\xxx\\");
            check("\\txxx", "\txxx");
            check("xxx\\t", "xxx\t");
            check("xxx\\txxx", "xxx\txxx");
            check("\\\\txxx", "\\txxx");
            check("\\\\\\txxx", "\\\txxx");
            check("xxx\\\\t", "xxx\\t");
            check("xxx\\\\\\t", "xxx\\\t");
            check("xxx\\\\txxx", "xxx\\txxx");
            check("xxx\\t\\", "xxx\t\\");
            check("xxx\\t\\xxx", "xxx\txxx");
            check("xxx\\t\\\\", "xxx\t\\");
        }
    }

    private final static char[] HEX_CHARS_L = "0123456789abcdef".toCharArray();
    private final static char[] HEX_CHARS_U = "0123456789ABCDEF".toCharArray();
    public static String toHexString(byte[] bytes, boolean upperCase)  {
        char[] hexChars = upperCase ? HEX_CHARS_U : HEX_CHARS_L;
        char[] result = new char [bytes.length*2];
        for (int i=0; i<bytes.length; i++)  {
            int v = bytes[i] & 0xFF;
            result[i * 2] = hexChars[v >>> 4];
            result[i * 2 + 1] = hexChars[v & 0x0F];
        }
        return new String (result);
    }


    public static <T extends Appendable> T replace(T result, String source, String target, String replacement) throws IOException
    {
        if (target.length()==0)  throw new IllegalArgumentException ("Empty target");

        for (int i=0; i!=source.length();)  {
            int i0 = i;
            i = source.indexOf(target, i);
            if (i==-1)  {
                result.append(i0==0 ? source : source.substring(i0));
                break;
            }
            if (i0!=i)  result.append(source.substring(i0, i));
            result.append(replacement);
            i += target.length();
        }
        return result;
    }

    public static class TestReplace
    {
        public static void check(String source, String traget, String replacement, String expect) throws Exception  {
            String result = replace(new StringBuilder (), source, traget, replacement).toString();
            System.out.println(result);
            if (!result.equals(expect))  throw new Exception ("is not equal to: "+expect);
        }
        public static void main(String[] args) throws Exception  {
            check("", "xxx", "", "");
            check("xxx", "xxx", "", "");
            check("xxx", "xxx", "yy", "yy");
            check("xxx", "xxxx", "yy", "xxx");
            check("xxxxxx", "xxx", "yy", "yyyy");
            check("axxxbbxxxc", "xxx", "yy", "ayybbyyc");
        }
    }

    public static String fillChars(char ch, int count)
    {
        char[] buffer = new char [count];
        Arrays.fill(buffer, ch);
        return new String (buffer);
    }

    //        ----    работа с путями    ----

    public static boolean startsWithPath(String path, String prefix, int offset)
    {
        return path.startsWith(prefix, offset) && (path.length()==prefix.length()+offset || path.charAt(prefix.length()+offset)=='/');
    }
    public static boolean startsWithPath(String path, String prefix)  {  return startsWithPath(path, prefix, 0);  }

    public static String cutStartPath(String path)
    {
        boolean absolute = path.startsWith("/");
        return path.substring(indexOf(path, '/', absolute ? 1 : 0) + (absolute ? 0 : 1));
    }

    public static String parentPath(String path)
    {
        return path.substring(0, path.lastIndexOf('/')+1);
    }


    //----------------        file        ----------------

    public static final Charset UTF8 = Charset.forName("UTF-8");
    public static final Charset UTF16LE = Charset.forName("UTF-16LE");
    public static final Charset UTF16BE = Charset.forName("UTF-16BE");
    public static final Charset UTF32LE = Charset.forName("UTF-32LE");
    public static final Charset UTF32BE = Charset.forName("UTF-32BE");

    public static final byte[] UTF8_BOM = new byte [] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
    public static final byte[] UTF16LE_BOM = new byte [] { (byte)0xFF, (byte)0xFE };
    public static final byte[] UTF16BE_BOM = new byte [] { (byte)0xFE, (byte)0xFF };
    public static final byte[] UTF32LE_BOM = new byte [] { (byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00 };
    public static final byte[] UTF32BE_BOM = new byte [] { (byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF };

    public static Charset charsetByBOM(byte[] bytes)  {
        if (ByteArray.startsWith(bytes, UTF8_BOM))  return Charset.forName("UTF-8");
        else if (ByteArray.startsWith(bytes, UTF16LE_BOM))  return Charset.forName("UTF-16LE");
        else if (ByteArray.startsWith(bytes, UTF16BE_BOM))  return Charset.forName("UTF-16BE");
        else if (ByteArray.startsWith(bytes, UTF32LE_BOM))  return Charset.forName("UTF-32LE");
        else if (ByteArray.startsWith(bytes, UTF32BE_BOM))  return Charset.forName("UTF-32BE");
        else  return null;
    }

    public static int cutBOM(byte[] bytes, String encoding)  {
        if (Charset.forName(encoding).equals(UTF8))  {  if (ByteArray.startsWith(bytes, UTF8_BOM))  return UTF8_BOM.length;  }
        else if (Charset.forName(encoding).equals(UTF16LE))  {  if (ByteArray.startsWith(bytes, UTF16LE_BOM))  return UTF16LE_BOM.length;  }
        else if (Charset.forName(encoding).equals(UTF16BE))  {  if (ByteArray.startsWith(bytes, UTF16BE_BOM))  return UTF16BE_BOM.length;  }
        else if (Charset.forName(encoding).equals(UTF32LE))  {  if (ByteArray.startsWith(bytes, UTF32LE_BOM))  return UTF32LE_BOM.length;  }
        else if (Charset.forName(encoding).equals(UTF32BE))  {  if (ByteArray.startsWith(bytes, UTF32BE_BOM))  return UTF32BE_BOM.length;  }
        return 0;
    }

    public static String read(InputStream input, String encoding) throws IOException  {
        ByteArray bytes = new ByteArray(input);
        return bytes.toString(cutBOM(bytes.data, encoding), encoding);
    }
    public static String read(RandomAccessFile file, String encoding) throws IOException  {
        byte[] bytes = ByteArray.read(file);
        int offset = cutBOM(bytes, encoding);
        return new String (bytes, offset, bytes.length-offset, encoding);
    }
    public static String read(File file, String encoding) throws IOException  {
        byte[] bytes = ByteArray.read(file);
        int offset = cutBOM(bytes, encoding);
        return new String (bytes, offset, bytes.length-offset, encoding);
    }
    public static String read(String fileName, String encoding) throws IOException  {
        return read(new File(fileName), encoding);
    }

    public static void write(String fileName, String content, String encoding, boolean append) throws IOException  {
        ByteArray.write(fileName, content.getBytes(encoding), append);
    }

    public static void write(String fileName, String content, String encoding) throws IOException  {
        ByteArray.write(fileName, content.getBytes(encoding));
    }

    public static void write(File file, String content, String encoding, boolean append) throws IOException  {
        ByteArray.write(file, content.getBytes(encoding), append);
    }

    public static void write(File file, String content, String encoding) throws IOException  {
        ByteArray.write(file, content.getBytes(encoding));
    }

    public static <E extends Throwable> void listFiles(File dir, Consumer<File, E> handler) throws E
    {
        File[] files = dir.listFiles();
        if (files==null) return;
        for (File file : files)
            if (file.isDirectory())  listFiles(file, handler);
            else  handler.process(file);
    }

    public static ArrayList<File> listFiles(File dir)
    {
        final ArrayList<File> result = new ArrayList<> ();
        listFiles(dir, new Consumer.R<File> () {
            public void process(File file)  {
                result.add(file);
            }
        });
        return result;
    }

    public static <E extends Throwable> void listFiles(String filename, Consumer<File, E> handler) throws E  {  listFiles(new File (filename), handler);  }
    public static ArrayList<File> listFiles(String filename)  {  return listFiles(new File (filename));  }

    public static String incFileName(String fileName, boolean lastExtension)  {
        //    определить позицию имени файла (последний слэш)
        int i0 = fileName.lastIndexOf('/') + 1;
        if (!File.separator.equals("/"))  {
            int i02 = fileName.lastIndexOf(File.separator) + 1;
            if (i02>i0)  i0 = i02;
        }
        //    определить позицию расширения файла (первая точка в имени файла)
        int i;
        if (lastExtension)  {  i = fileName.lastIndexOf('.');  if (i<i0)  i = fileName.length();  }
        else  i = indexOf(fileName, '.', i0);
        //    найти индекс в скобках
        if (i>=4 && fileName.charAt(i-1)==')')  {
            int i2 = fileName.lastIndexOf('(', i-2);
            if (i2>i0)
                try  {  int count = Integer.parseInt(fileName.substring(i2+1, i-1));
                        return fileName.substring(0, i2+1) + (count+1) + fileName.substring(i-1);  }
                catch (NumberFormatException e)  {}
        }
        //    если не найден, вернуть с индексом 2
        return fileName.substring(0, i) + " (2)" + fileName.substring(i);
    }
    public static String incFileName(String fileName)  {  return incFileName(fileName, false);  }

    public static String incFileNameWhileExists(String fileName, boolean lastExtension)  {
        while (new File (fileName).exists())  fileName = incFileName(fileName, lastExtension);
        return fileName;
    }

    public static String addFileName(String fileName, String suffix, boolean lastExtension)  {
        //    определить позицию имени файла (последний слэш)
        int i0 = fileName.lastIndexOf('/') + 1;
        if (!File.separator.equals("/"))  {
            int i02 = fileName.lastIndexOf(File.separator) + 1;
            if (i02>i0)  i0 = i02;
        }
        //    определить позицию расширения файла (первая точка в имени файла)
        int i;
        if (lastExtension)  {  i = fileName.lastIndexOf('.');  if (i<i0)  i = fileName.length();  }
        else  i = indexOf(fileName, '.', i0);
        //    добавить суффикс к имени
        return fileName.substring(0, i) + suffix + fileName.substring(i);
    }
    public static String addFileName(String fileName, String suffix)  {  return addFileName(fileName, suffix, false);  }

    public static void mkdir(File file) throws IOException  {
        if (!file.mkdir())  throw new IOException ("Can't make a new directory "+file);
    }

    public static void delete(File file) throws IOException  {
        if (!file.delete())  throw new IOException ("Can't delete a file or directory "+file);
    }

    public static void renameTo(File src, File dst) throws IOException  {
        if (!src.renameTo(dst))  throw new IOException ("Can't rename "+src+" to "+dst);
    }
}


