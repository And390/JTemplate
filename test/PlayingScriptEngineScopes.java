import org.junit.Test;
import ru.and390.template.TemplateManager;

import javax.script.*;

/**
 * And390 - 22.04.2015
 */
public class PlayingScriptEngineScopes
{
    static ScriptEngine engine = TemplateManager.getEngine();

    public static void printScopesValues(String name)
    {
        System.out.println("engine scope:          "+engine.getBindings(ScriptContext.ENGINE_SCOPE).get(name));
        System.out.println("global scope:          "+engine.getBindings(ScriptContext.GLOBAL_SCOPE).get(name));
        System.out.println("context engine scope:  "+engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).get(name));
        System.out.println("context global scope:  "+engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE).get(name));
    }

    public static void printScopesValues(String name, Bindings bindings)
    {
        printScopesValues(name);
        System.out.println("bindings:              "+bindings.get(name));
    }

    private static Object get(Bindings bindings, String name)  {  return bindings==null ? null : bindings.get(name);  }

    public static void printScopesValues(String name, ScriptContext context)
    {
        printScopesValues(name);
        System.out.println("user engine scope:     " + get(context.getBindings(ScriptContext.ENGINE_SCOPE), name));
        System.out.println("user global scope:     " + get(context.getBindings(ScriptContext.GLOBAL_SCOPE), name));
    }

    @Test
    public void play() throws ScriptException
    {
        String script = "if (typeof a == 'undefined') a=0; a++; java.lang.System.out.println('from script:           '+a);";

        // значение сохранится в context, но для Nashorn значение в java не вернется (только в Rhino)
        System.out.println("\n    With new SimpleScriptContext ()");
        ScriptContext context = new SimpleScriptContext ();
        engine.eval(script, context);
        printScopesValues("a", context);
        System.out.println("  second");
        engine.eval(script, context);
        printScopesValues("a", context);

        // значение сохранится в bindings, но для Nashorn значение в java не вернется (только в Rhino)
        System.out.println("\n    With new SimpleBindings ()");
        Bindings bindings = new SimpleBindings ();
        engine.eval(script, bindings);
        printScopesValues("a", bindings);
        System.out.println("  second");
        engine.eval(script, bindings);
        printScopesValues("a", bindings);

        // для Nashorn только этот способ позволит получить значение a
        System.out.println("\n    With ScriptEngine.createBindings ()");
        bindings = engine.createBindings();
        engine.eval(script, bindings);
        printScopesValues("a", bindings);
        System.out.println("  second");
        engine.eval(script, bindings);
        printScopesValues("a", bindings);

        System.out.println("\n    Without");
        engine.eval(script);
        printScopesValues("a");
        System.out.println("  second");
        engine.eval(script);
        printScopesValues("a");

        System.out.println("\n    With new SimpleScriptContext ()");
        context = new SimpleScriptContext ();
        engine.eval(script, context);
        printScopesValues("a", context);
        System.out.println("  second");
        engine.eval(script, context);
        printScopesValues("a", context);

        System.out.println("\n    With new SimpleBindings ()");
        bindings = new SimpleBindings ();
        engine.eval(script, bindings);
        printScopesValues("a", bindings);
        System.out.println("  second");
        engine.eval(script, bindings);
        printScopesValues("a", bindings);
    }

    @Test
    public void includeVariable() throws ScriptException
    {
        final ScriptContext context = new SimpleScriptContext ();
        final Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("obj", new Object () {
            public int hashCode() {
                System.out.println(bindings.get("a"));
                try  {  engine.eval("a++", context);  }
                catch (ScriptException e)  {  throw new RuntimeException (e);  }
                return 0;
            }
        });
        engine.eval("a=1; obj.hashCode();", context);
        System.out.println(bindings.get("a"));
    }
}
