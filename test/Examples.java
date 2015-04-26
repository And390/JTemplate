import ru.and390.template.FileTemplateManager;
import ru.and390.template.TemplateManager;

import javax.script.Bindings;
import javax.script.SimpleBindings;

/**
 * And390 - 21.04.2015
 */
public class Examples
{
    public static void main(String[] args) throws Exception
    {
        String templateContent = "Hello ${name}!";
        Bindings values = TemplateManager.createBindings();
        values.put("name", "World");
        TemplateManager.parse(templateContent).eval(values, System.out);
    }

    public static class LoopsAndBranches
    {
        public static void main(String[] args) throws Exception
        {
            String[] names = new String[] {
                    "no one", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
            int value = (int)(Math.random()*(names.length - 1 + names.length/2)) + 1 - names.length/2;

            String templateContent =
                    "How much items?\n" +
                    "<$ if (value<=0) { $> > ${names[0]}\n" +
                    "<$ } else for (var i=1; i<=value; i++) { $> > ${names[i]}\n<$ } $>";
            Bindings values = new SimpleBindings ();
            values.put("value", value);
            values.put("names", names);
            TemplateManager.parse(templateContent).eval(values, System.out);
        }
    }

    public static class ToString
    {
        public static void main(String[] args) throws Exception
        {
            String templateContent =
                    "${ a = { toString: function() { return 'hello'; } }; a }";
            Bindings values = new SimpleBindings ();
            TemplateManager.parse(templateContent).eval(values, System.out);
        }
    }

    public static class Functions
    {
        public String quote(String string)  {  return "\""+string.replace('\"', '\'')+"\"";  }

        public static void main(String[] args) throws Exception
        {
            String templateContent =
                    "Julius Caesar: ${util.quote('I assure you I had rather be the first man here than the second man in Rome')}";
            Bindings values = TemplateManager.createBindings();
            values.put("util", new Functions ());
            TemplateManager.parse(templateContent).eval(values, System.out);
        }
    }

    public static class FileManager
    {
        public static void main(String[] args) throws Exception
        {
            FileTemplateManager manager =  new FileTemplateManager ();  // we can pass new File ("testweb") and use relative template path
            Bindings values = TemplateManager.createBindings();
            values.put("contextPath", "");
            values.put("user", null);
            values.put("error", null);
            manager.eval("/testweb/hello.html", values, System.out);
        }
    }
}
