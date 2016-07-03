package ru.and390.template;

import ru.and390.utils.FileWatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * В дополнение к FileTemplateManager мониторит заданный каталог, выгружая измененные шаблоны из кэша
 * And390 - 21.04.2015
 */
public class WatchFileTemplateManager extends FileTemplateManager implements AutoCloseable
{
    private FileWatcher watcher;

    public WatchFileTemplateManager(File file, String encoding) throws IOException
    {
        this(file, Charset.forName(encoding));
    }

    public WatchFileTemplateManager(File file, Charset charset) throws IOException
    {
        super(file, charset);
        watcher = new FileWatcher (file.getPath());
        watcher.addListener(new FileWatcher.Listener ()  {
            public void changed(String path)  {
                putTemplate(path, null);
            }
        });
    }

    public void close() throws IOException, InterruptedException
    {
        watcher.close();
    }
}
