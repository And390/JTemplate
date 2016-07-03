package ru.and390.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

/**
 * And390 - 29.09.2011
 */
public class ByteArray {

    public interface FindHandler {
        void targetFinded(byte[] source, int begin, int end)  throws Exception;
        void sourceFinded(byte[] source, int begin, int end)  throws Exception;
    }

    //в source ищет open и следующий за ним close, и для каждого найденного куска, заключенного в open и close,
    //  вызывает handleTarget, для оставшихся кусков вызывает handeSource
    public static void findPart(byte[] source, int sourceOffset, int sourceEnd,
                                byte[] open, int openOffset, int openEnd,
                                byte[] close, int closeOffset, int closeEnd,
                                FindHandler handler)  throws Exception
    {
        int openLength = openEnd - openOffset;
        int closeLength = closeEnd - closeOffset;
        int i0=sourceOffset;
        while (true)
        {
            int i1 = indexOf(source, i0, sourceEnd, open, openOffset, openEnd);
            if (i1==-1)  break;
            int i2 = indexOf(source, i1+openLength, sourceEnd, close, closeOffset, closeEnd);
            if (i2==-1)  break;
            i2 += closeLength;
            handler.sourceFinded(source, i0, i1);
            handler.targetFinded(source, i1, i2);
            i0=i2;
        }
        handler.sourceFinded(source, i0, sourceEnd);
    }

    public static void findPart(byte[] source, int sourceOffset, int sourceEnd,
                                byte[] begin, byte[] end, FindHandler handler) throws Exception {
        findPart(source, sourceOffset, sourceEnd, begin, 0, begin.length, end, 0, end.length, handler);
    }
    
    public static void findPart(byte[] source, byte[] begin, byte[] end, FindHandler handler) throws Exception {
        findPart(source, 0, source.length, begin, 0, begin.length, end, 0, end.length, handler);
    }

    //----------------        аналог String.indexOf        ----------------

    //немного переделанная функция из jdk String.indexOf
    public static int indexOf(byte[] source, int sourceOffset, int sourceEnd,
                              byte[] target, int targetOffset, int targetCount)
    {
        byte first  = target[targetOffset];
        int max = sourceEnd - targetCount;

        for (int i = sourceOffset; i <= max; i++) {
            //Look for first character.
            if (source[i] != first)  while (++i <= max && source[i] != first);
            //Found first character, now look at the rest of v2
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j]==target[k]; j++, k++);

                if (j == end)  return i;  //Found whole string.
            }
        }
        return -1;
    }

    public static int indexOf(byte[] source, int sourceOffset, int sourceEnd, byte[] target) {
        return indexOf(source, sourceOffset, sourceEnd, target, 0, target.length);
    }

    public static int indexOf(byte[] source, byte[] target, int offset) {
        return indexOf(source, offset, source.length, target, 0, target.length);
    }

    public static int indexOf(byte[] source, byte[] target) {
        return indexOf(source, 0, source.length, target, 0, target.length);
    }

    public static int lastIndexOf(byte[] source, int sourceOffset,
                                  byte[] target, int targetOffset, int targetCount,
                                  int fromIndex)
    {
        int strLastIndex = targetOffset + targetCount - 1;
        byte strLastChar = target[strLastIndex];
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

        startSearchForLastChar:
	    while (true)
        {
            while (i >= min && source[i] != strLastChar)  i--;
            if (i < min)  return -1;
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;

            while (j > start)
                if (source[j--] != target[k--]) {
                    i--;
                    continue startSearchForLastChar;
                }

            return start - sourceOffset + 1;
        }
    }

    public static int lastIndexOf(byte[] source, int sourceOffset, byte[] target, int fromIndex) {
        return lastIndexOf(source, sourceOffset, target, 0, target.length, fromIndex);
    }

    public static int lastIndexOf(byte[] source, byte[] target, int fromIndex) {
        return lastIndexOf(source, 0, target, 0, target.length, fromIndex);
    }


    //----------------        аналог String.startsWith и String.endsWith        ----------------

    public static boolean startsWith(byte[] source, int sourceOffset, int sourceEnd,
                                     byte[] target, int targetOffset, int targetEnd)
    {
        // с точки зрения реализации этой функции было бы удобнее передавать коичество, а не конец,
        // но с точки зрения дизайна лучше сделать по аналогии с indexOf
        int sourceCount = sourceEnd - sourceOffset;
        int targetCount = targetEnd - targetOffset;
        if (sourceCount < targetCount)  return false;
        while (--targetCount >= 0)  if (source[sourceOffset++] != target[targetOffset++])  return false;
        return true;
    }

    public static boolean startsWith(byte[] source, byte[] target)  {
        return startsWith(source, 0, source.length, target, 0, target.length);
    }

    public static boolean endsWith(byte[] source, int sourceOffset, int sourceEnd,
                                   byte[] target, int targetOffset, int targetEnd)
    {
        return startsWith(source, sourceEnd-(targetEnd-targetOffset), source.length, target, targetOffset, target.length);
    }

    public static boolean endsWith(byte[] source, byte[] target)  {
        return startsWith(source, 0, source.length, target, 0, target.length);
    }


    //----------------        diff        ----------------

    public static byte[] diff(byte[] oldString, byte[] newString)
    {
        int l1 = oldString.length;
        int l2 = newString.length;
        int[] n = new int [l1 * l2];

        for (int i=0; i<l1; i++)
        {
            int lastnij = 0;
            int lastnj = 0;
            for (int j=0; j<l2; j++)
            {
                int lastni = i==0 ? 0 : n [i-1 + l1*j];

                int newn;

                if (oldString[i]==newString[j])  newn = lastnij + 1;
                else
                {
                    newn = lastni>lastnj ? lastni : lastnj;
                }

                n [i + j*l1] = newn;

                lastnij = lastni;
                lastnj = newn;
            }
        }

        //print
//        System.out.print(" ");
//        for (int j=0; j<l2; j++)  System.out.print(" "+BYTE_STRINGS[oldString[j]]);
//        System.out.println();
//        for (int i=0; i<l1; i++)
//        {
//            System.out.print(BYTE_STRINGS[newString[i]]);
//            for (int j=0; j<l2; j++)  System.out.print(" " + n [i + j*l1]);
//            System.out.println();
//        }
//        System.out.println();

        //result
        byte result[] = new byte [n[l1*l2-1]];
        int _n = result.length;

        for (int i=l1-1,j=l2-1; ; )
        {
            if (oldString[i]==newString[j])
            {
                _n--;
                result[_n] = oldString[i];
                if (_n==0)  break;
                i--;  j--;
            }
            else
            {
                if (n [i-1 + j*l1] == _n && i!=0)  i--;
                else  j--;
            }
            if(_n!=n[i + j*l1])  throw new RuntimeException ("debug");
        }

        return result;
    }

    //----------------        toString        ----------------

    private static final char[] DIGITS = new char [] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'  };
    private static final String[] BYTE_STRINGS = new String [256];
    static  {  for (int i=0; i<256; i++)  BYTE_STRINGS[i] = new String (new char[] {  DIGITS[i/16], DIGITS[i%16]  });  }

    public static String toString(byte value)  {  return BYTE_STRINGS[value];  }

    public static String toString(byte[] data)
    {
        char[] buffer = new char [data.length*2];
        int i=0;
        for (byte _value : data) {
            int value = _value & 0xFF;  //чтобы значения > 127 не были отрицательными числами
            buffer[i] = DIGITS[value/16];  i++;
            buffer[i] = DIGITS[value%16];  i++;
        }
        return new String (buffer, 0, buffer.length);
    }

    //----------------        allocation        ----------------

    private static final int START_CAPACITY = 1024 - 16;

    // увеличивает объем памяти в два раза, но не меньше переданного значения и не меньше START_CAPACITY
    public static byte[] realloc(byte[] data, int size)
    {
        int capacity = data.length * 2;
        if (capacity<size)  capacity = size;
        if (capacity<START_CAPACITY)  capacity = START_CAPACITY;
        return Arrays.copyOf(data, capacity);
    }

    //----------------        object        ----------------

    public byte[] data;
    public int size;

    public int capacity()  {  return data.length;  }
    public int remaining()  {  return data.length - size;  }

    public ByteArray()  {  data = new byte [START_CAPACITY];  size=0;  }
    public ByteArray(int size)  {  data = new byte [Math.max(size, START_CAPACITY)];  this.size=size;  }
    public ByteArray(int size, int capacity)  {  data = new byte [Math.max(size, capacity)];  this.size=size;  }
    public ByteArray(byte[] _data)  {  data=_data;  size=data.length;  }
    public ByteArray(byte[] _data, int _size)  {  data=_data;  size=_size;  }

    public void realloc()  {  data=realloc(data, size);  }

    public void provide(int count)  {  count+=size;  if (data.length<count)  data=realloc(data, count);  }

    public String toString(String encoding) throws UnsupportedEncodingException  {  return new String (data, 0, size, encoding);  }
    public String toString(int offset, Charset charset) throws UnsupportedEncodingException  {  return new String (data, offset, size-offset, charset);  }

    //----------------        io        ----------------

    // читает поток целиком в массив байтов
    public static byte[] read(InputStream input) throws IOException
    {
        return read(input, input.available());
    }

    // эффективный вариант, если известен размер (например, ZipInputStream) точно или хотя бы предположительно
    public static byte[] read(InputStream input, int expectedSize) throws IOException
    {
        // цикл идет в двух режимах: когда размер буфера (data.length) равен expectedSize,
        // при заполнении буфера проверяется, есть ли еще данные в потоке и перевыделение буфера идет с учетом available(),
        // (если она вернет значение меньше удвоенного размера буфера, то будет переключение на более простой второй режим)
        // когда не равен, размер буфера просто всегда удваивается (первый режим может сократить количество realloc-ов)
        byte[] data = new byte [expectedSize<2 ? START_CAPACITY : expectedSize];
        int size = 0;
        for (;;)  {
            int readed = input.read(data, size, data.length-size);
            if (readed==-1)  break;
            size += readed;
            if (size==data.length)
                if (size==expectedSize)  {
                    readed = input.read();
                    if (readed==-1)  break;
                    expectedSize = size + 1 + input.available();
                    data = realloc(data, expectedSize);
                    data[size++] = (byte) readed;
                }
                else  {
                    data = realloc(data, size);
                }
        }
        if (size!=data.length)  return Arrays.copyOf(data, size);
        return data;
    }

    // количество прочитанных байт должно быть равно в точности
    public static byte[] readFixed(InputStream input, int expectedSize) throws IOException
    {
        byte[] data = new byte [expectedSize];
        for (int size=0;;)  {
            int readed = input.read(data, size, expectedSize-size);
            if (readed==-1)  throw new RuntimeException ("Unexpected end of stream: readed "+size+" bytes, expected "+expectedSize+" bytes");
            size += readed;
            if (size==expectedSize)  {
                if (input.read()!=-1)  throw new RuntimeException ("Stream has more data than expected: "+expectedSize+" bytes");
                return data;
            }
        }
    }

    //    startCapacity эффективнее передавать на 1 больше известного размера данных
    public ByteArray(InputStream input) throws IOException  {  this(input, START_CAPACITY);  }
    public ByteArray(InputStream input, int startCapacity) throws IOException
    {
        data = new byte [Math.max(input.available()+1, startCapacity)];
        for (;;)  {
            int readed = input.read(data, size, data.length-size);
            if (readed==-1)  break;
            size += readed;
            if (size==data.length)  data = realloc(data, size);
        }
    }

    public static byte[] read(String fileName) throws IOException  {  return read(new File(fileName));  }
    public static byte[] read(File file) throws IOException
    {
        long size = file.length();
        FileInputStream input = new FileInputStream (file);
        try  {
            if (size>Integer.MAX_VALUE)  throw new IOException ("File is too large: "+size+" bytes");
            return readFixed(input, (int)size);
        }
        finally  {  input.close();  }
    }
    public static byte[] read(RandomAccessFile file) throws IOException
    {
        long size = file.length();
        if (size>Integer.MAX_VALUE)  throw new IOException ("File is too large: "+size+" bytes");
        byte[] data = new byte [(int)size];
        file.read(data);
        return data;
    }

    public void write(OutputStream out) throws IOException  {  out.write(data, 0, size);  }

    public static void write(String fileName, byte[] data) throws IOException  {  write(new File(fileName), data, false);  }
    public static void write(String fileName, byte[] data, boolean append) throws IOException  {  write(new File(fileName), data, append);  }
    public static void write(File file, byte[] data) throws IOException  {  write(file, data, false);  }
    public static void write(File file, byte[] data, boolean append) throws IOException
    {
        FileOutputStream output = new FileOutputStream(file, append);
        try  {  output.write(data);  }
        finally  {  output.close();  }
    }

    public void write(String fileName) throws IOException  {  write(new File(fileName));  }
    public void write(File file) throws IOException
    {
        FileOutputStream output = new FileOutputStream(file);
        try  {  output.write(data, 0, size);  }
        finally  {  output.close();  }
    }


    public static class TestRead
    {
        private static Random random = new Random();
        private static int rand()  {  return 0x7FFFFFFF / (0x7FFFFFFF & random.nextInt());  }

        public static void main(String[] args) throws Exception  {
            //    заполнить случайный буфер
            byte[] source = new byte [32*1024];
            for (int i=0; i<source.length; i++)  source[i] = (byte) rand();
            //    протетстировать ByteArray
            ByteArray buffer = new ByteArray(new TestInputStream(source), 16);
            test(buffer.data, buffer.size, source);
            //    протестировать функцию read
            byte[] data = read(new TestInputStream(source));
            test(data, data.length, source);
            //    протестировать функцию read с заданным размером
            data = readFixed(new TestInputStream(source), source.length);
            test(data, data.length, source);
        }

        public static void test(byte[] result, int size, byte[] expected)  {
            if (size!=expected.length)  throw new RuntimeException ("not match");
            for (int i=0; i<size; i++)  if (result[i]!=expected[i])  throw new RuntimeException ("not match");
        }

        public static class TestInputStream extends ByteArrayInputStream {
            public TestInputStream(byte[] array)  {  super(array);  }
            public int available() {  return Math.min(rand(), super.available());  }
            public int read(byte[] data, int offset, int size)  {  return super.read(data, offset, Math.min(rand(), size));  }
        }

        public static class DumpInputStream extends InputStream
        {
            public final InputStream input;
            public DumpInputStream(InputStream input_)  {  input=input_;}
            @Override
            public int read() throws IOException  {
                System.out.println("read byte");
                return input.read();
            }
            //public int read(byte[] b) throws IOException  правильно реализован в InputStream
            @Override
            public int read(byte[] b, int off, int len) throws IOException  {
                int result = input.read(b, off, len);
                System.out.println("read "+result+" bytes of "+len+" in buffer");
                return result;
            }
            @Override
            public long skip(long n) throws IOException  {
                long result = input.skip(n);
                System.out.println("skip "+result+" bytes of "+n);
                return result;
            }
            @Override
            public int available() throws IOException  {
                int result = input.available();
                System.out.println("available "+result+" bytes");
                return result;
            }
            @Override
            public void close() throws IOException  {  input.close();  }
            @Override
            public synchronized void mark(int readlimit)  {  input.mark(readlimit);  }
            @Override
            public synchronized void reset() throws IOException  {  input.reset();  }
            @Override
            public boolean markSupported()  {  return input.markSupported();  }
        }
    }

    public static class TestReadProcessInputStream
    {
        // Process.getInputStream.read() не возвращает -1 если в буфере больше нет места для записи, вернет 0

        public static void main(String[] args) throws Exception
        {
            Process process = Runtime.getRuntime().exec("help");
//            ByteArray byteArray = new ByteArray (new TestRead.DumpInputStream (process.getInputStream()), 4515+1);
            ByteArray byteArray = new ByteArray (read(new TestRead.DumpInputStream (process.getInputStream())));
            System.out.println(new String (byteArray.data, 0, byteArray.size, "cp866"));
        }
    }

    public static class TestZipInputStream
    {
        // если в качестве startCapacity указать известный размер zip-сущности, то метод лишний раз выделит память в конце,
        // так как available() для него возвращает 1, даже когда достигнут конец

        private static Random random = new Random();

        public static void main(String[] args) throws Exception
        {
            // ZipOutputStream не сохраняет размеры сущностей
//            //    test data
//            byte[] bytes = new byte [4*1024];
//            random.nextBytes(bytes);
//
//            //    write test zip
//            ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream ();
//            java.util.zip.ZipOutputStream zipOutput = new java.util.zip.ZipOutputStream (arrayOutput);
//            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry ("entry");
//            zipOutput.putNextEntry(zipEntry);
//            zipOutput.write(bytes);
//            zipOutput.closeEntry();
//            zipOutput.close();

            //    read this zip
            java.util.zip.ZipInputStream zipInput = new java.util.zip.ZipInputStream (new FileInputStream ("test.zip"));
            java.util.zip.ZipEntry zipEntry = zipInput.getNextEntry();
            System.out.println("size: "+zipEntry.getSize());
            System.out.println("compressed size: "+zipEntry.getCompressedSize());
            byte[] byteArray = readFixed(new TestRead.DumpInputStream(zipInput), (int) zipEntry.getSize());
            System.out.println(new String (byteArray, "cp1251"));
        }
    }

    public static class TestDiff
    {
        public static void main(String[] args)
        {
            String oldstr = "Header,20120127185120_237445,,,O,2012-01-24,FS,AO,C,T,,,Consumer,1968-11-26,,,,,  ,Account,7,1,2008-05-22,0,2006-04-01,2006-04-01,36599,23881,1958,1,RUR,2058-05-22,2008-05-22,,0,3,,6119,Account,7,1,2008-05-22,52,,2011-12-28,22508,0,2480,0,RUR,2058-05-22,2008-05-22,,32738,3,,30000,Account,7,1,2007-09-26,52,2006-09-30,2006-09-30,6984,6706,,0XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX,RUB,,2010-10-20,,1503,0,,-6706,Address, ,426009,  ,1,0001-01-01,2008-06-09,  ,Address, ,426009,  ,2,0001-01-01,2008-06-09,  ,Address, ,426009,18,1,0001-01-01,2010-10-22,RU,Address, ,426009,18,2,0001-01-01,2010-10-22,RU";
            String newstr = "Header,20120127191721_237445,,,O,2010-10-20,FS,AO,C,T,,,Consumer,1968-11-26,,,,,  ,Account,7,1,2008-05-22,0,2006-04-01,2006-04-01,36599,23881,1958,1,RUR,2058-05-22,2008-05-22,,0,3,,1958,Account,7,1,2008-05-22,52,,2010-09-29,22508,0,2480,0,RUR,2058-05-22,2008-05-22,,32738,3,,2480,Account,7,1,2007-09-26,52,2006-09-30,2006-09-30,6984,6706,,XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX,RUB,,2010-09-01,,1503,0,,0,Address, ,426009,  ,1,0001-01-01,2008-06-09,  ,Address, ,426009,  ,2,0001-01-01,2008-06-09,";
            String expect = "Header,20120127112_237445,,,O,201-0-2,FS,AO,C,T,,,Consumer,1968-11-26,,,,,  ,Account,7,1,2008-05-22,0,2006-04-01,2006-04-01,36599,23881,1958,1,RUR,2058-05-22,2008-05-22,,0,3,,19,Account,7,1,2008-05-22,52,,201--2,22508,0,2480,0,RUR,2058-05-22,2008-05-22,,32738,3,,0,Account,7,1,2007-09-26,52,2006-09-30,2006-09-30,6984,6706,,XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX,RUB,,2010-0-0,,1503,0,,0,Address, ,426009,  ,1,0001-01-01,2008-06-09,  ,Address, ,426009,  ,2,0001-01-01,2008-06-09,";
            byte[] result = diff(oldstr.getBytes(), newstr.getBytes());
            System.out.println(new String(result));
            System.out.println(new String(result).equals(expect) ? "success, match" : "error, do not match");
        }
    }
}
