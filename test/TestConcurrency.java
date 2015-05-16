import org.junit.Test;
import static org.junit.Assert.*;
import ru.and390.template.Template;
import ru.and390.template.TemplateManager;
import ru.and390.utils.StringList;

import javax.script.*;
import java.util.List;
import java.util.Map;

/**
 * And390 - 22.04.2015
 */
public class TestConcurrency
{
    ScriptEngine jsEngine = new ScriptEngineManager ().getEngineByName("JavaScript");
    {
        System.out.println("JavaScript engine: "+jsEngine.getFactory().getEngineName());
    }

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

    @Test
    public void scriptEngine() throws Exception
    {
        final String script = "for (var i=0; i<1000; i++)  ;  java.lang.System.out.println(i);";
        final CompiledScript compiled = ((Compilable)jsEngine).compile(script);
        Thread[] threads = new Thread [50];
        for (int i=0; i<threads.length; i++) {
            threads[i] = new Thread () {
                @Override
                public void run() {
                    try {
                        jsEngine.eval(script, new SimpleBindings ());  //is this code thread-safe?
                        compiled.eval(new SimpleBindings ());  //and this?
                    }
                    catch (Exception e)  {  throw new RuntimeException (e);  }
                }
            };
            threads[i].start();
        }
        for (int i=0; i<threads.length; i++)  threads[i].join();
    }

    @Test
    public void compiledScript() throws Exception
    {
        final String script = "a=0;  for (var i=0; i<1000; i++)  a++;  java.lang.System.out.println(a);";

        Thread[] threads = new Thread [50];
        for (int i=0; i<threads.length; i++)  {
            threads[i] = new Thread () {
                @Override
                public void run()  {
                    try  {  jsEngine.eval(script, new SimpleBindings ());  //does this code thread-safe???
                    } catch (Exception e)  {  throw new RuntimeException (e);  }
                }
            };
            threads[i].start();
        }
        for (int i=0; i<threads.length; i++)  threads[i].join();
    }

    // Конкурентное использование Bindings движком обычно не корректно.
    // Во-первых нужно потоко-безопасным должен быть сам Bindings. Во-вторых, реализация ScriptEngine.
    // Для Rhino (.getFactory().getParameter("THREADING")=MULTITHREADED):
    //    The engine implementation is internally thread-safe and scripts may execute concurrently
    //    although effects of script execution on one thread may be visible to scripts on other threads (!)
    // и в итоге код ниже не выводит корректное число из-за того, что a++ не атомарно.
    // Для Nashorn (.getFactory().getParameter("THREADING")=null):
    //    The engine implementation is not thread safe, and cannot be used to execute scripts concurrently on multiple threads
    // это код кидает Exception
    @Test
    public void sharedBindingsError() throws Exception
    {
        final Bindings bindings = new SynchronizedBindings (jsEngine.createBindings());
        bindings.put("a", 0);

        Thread[] threads = new Thread [10];
        for (int i=0; i<threads.length; i++)  {
            final String vi = "i"+i;
            threads[i] = new Thread () {
                @Override
                public void run()  {
                    try  {  jsEngine.eval("for (var i=0; i<1000; i++)  a++;", bindings);  // a++ и использование i не потокобезопасны
                    } catch (Exception e)  {  throw new RuntimeException (e);  }
                }
            };
            threads[i].start();
        }
        for (int i=0; i<threads.length; i++)  threads[i].join();

        System.out.println(bindings.get("a"));
    }

    // Параллельное выполнение разных шаблонов (текст не должен смешиваться)
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