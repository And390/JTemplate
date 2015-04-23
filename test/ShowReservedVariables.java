import org.junit.Test;
import ru.and390.template.TemplateManager;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * And390 - 22.04.2015
 */
public class ShowReservedVariables
{
    @Test
    public void show() throws ScriptException
    {
        Bindings bindings = new SimpleBindings();
        TemplateManager.getEngine().eval("", bindings);
        for (String key : bindings.keySet())  System.out.println(key+": "+bindings.get(key));
    }

    @Test
    public void context() throws Exception
    {
        SimpleBindings bindings = new SimpleBindings ();
        bindings.put("context", "value that you can not see");
        TemplateManager.parse("${context}").eval(bindings, System.out);
    }
}
