import org.junit.Test;
import static org.junit.Assert.*;
import ru.and390.template.Template;
import ru.and390.template.TemplateManager;
import ru.and390.utils.StringList;

import javax.script.*;
import java.util.List;

/**
 * And390 - 22.04.2015
 */
public class TestConcurrency
{
    ScriptEngine jsEngine = TemplateManager.getEngine();
    public StringList result = new StringList ();

    @Test
    public void printEngine()
    {
        List<ScriptEngineFactory> factories = new ScriptEngineManager ().getEngineFactories();
        for (ScriptEngineFactory factory : factories)  {
            System.out.println(String.format(
                    "engineName: %s, THREADING: %s",
                    factory.getEngineName(), factory.getParameter("THREADING")));
        }
    }

    // Параллельное использование переменной движком
    @Test
    public void variable() throws Exception
    {
        final Bindings bindings = jsEngine.createBindings();
        bindings.put("a", 0);

        Thread[] threads = new Thread [10];
        for (int i=0; i<threads.length; i++)  {
            threads[i] = new Thread () {
                @Override
                public void run()  {
                    try  {  jsEngine.eval("for (var i=0; i<1000; i++)  a++;", bindings);
                    } catch (Exception e)  {  throw new RuntimeException (e);  }
                }
            };
            threads[i].start();
        }
        for (int i=0; i<threads.length; i++)  threads[i].join();

        System.out.println(bindings.get("a"));
    }

    // Параллельное выполнение разных шаблонов на одном движке (текст не должен смешиваться)
    @Test
    public void templates() throws Exception
    {
        Template[] templates = new Template [] {
            TemplateManager.parse("A1; ${java.lang.Thread.sleep(200);}A2; ${java.lang.Thread.sleep(600);}A3; "),
            TemplateManager.parse("B1; ${java.lang.Thread.sleep(200);}B2; ${java.lang.Thread.sleep(200);}B3; ")
        };
        Thread[] threads = new Thread [templates.length];
        for (int i=0; i<threads.length; i++)  {
            final Template template = templates[i];
            threads[i] = new Thread () {
                @Override
                public void run()  {
                    try  {  template.eval(new SimpleBindings (), result);  }
                    catch (Exception e)  {  throw new RuntimeException (e);  }
                }
            };
            threads[i].start();
            Thread.sleep(100);
        }
        for (Thread thread : threads)  thread.join();
        assertEquals("A1; B1; A2; B2; B3; A3; ", result.toString());
    }
}