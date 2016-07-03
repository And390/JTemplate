package ru.and390.template;

import ru.and390.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Реализует CacheTemplateManager, загружая контент из файлов по указанным путям
 * And390 - 20.04.2015
 */
public class FileTemplateManager extends CacheTemplateManager
{
    public final File root;
    public final Charset charset;

    public FileTemplateManager()  {  this(new File ("."));  }
    public FileTemplateManager(File root)  {  this(root, Charset.defaultCharset().name());  }
    public FileTemplateManager(String encoding)  {  this(new File ("."), encoding);  }
    public FileTemplateManager(Charset charset)  {  this(new File ("."), charset);  }
    public FileTemplateManager(File root_, String encoding_)  {  root=root_;  charset=Charset.forName(encoding_);  }
    public FileTemplateManager(File root_, Charset charset_)  {  root=root_;  charset=charset_;  }

    protected String readContent(String path) throws IOException  {
        try  {  return Util.read(new File (root, path), charset);  }
        catch (FileNotFoundException e)  {  return null;  }
    }
}
