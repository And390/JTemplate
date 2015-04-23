import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import junit.framework.Assert;
import org.junit.Test;
import ru.and390.template.Template;
import ru.and390.template.TemplateManager;
import ru.and390.utils.StringList;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.HashMap;

/**
 * And390 - 22.04.2015
 */
public class TestTemplate
{
    public StringList result = new StringList ();

    public void check(Template template, String expected) throws Exception
    {
        template.eval(new SimpleBindings (), result);
        assertEquals(expected, result.toString());
        result.clear();
    }

    public void check(String source, String expected) throws Exception
    {
        check(TemplateManager.parse(source), expected);
    }

    @Test
    public void hello() throws Exception
    {
        Bindings values = new SimpleBindings();
        values.put("name", "World");
        TemplateManager.parse("Hello ${name}!").eval(values, result);
        assertEquals("Hello World!", result.toString());
    }

    @Test
    public void staticTemplate() throws Exception
    {
        String text = "Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit";
        Template template = TemplateManager.parse(text);
        assertTrue("template is not a StaticTemplate", template instanceof TemplateManager.StaticTemplate);
        template.eval(new SimpleBindings(), result);
        assertEquals(text, result.toString());
    }

    @Test
    public void comments() throws Exception
    {
        check("${/**/} ", " ");
        check("${1/**/} ", "1 ");
        check("${ /**/ 2 } ", "2 ");
        check("${3;/**/} ", " ");
        check("${4/**/;} ", " ");
        check("${/**/5;} ", " ");
        check("${6;/**/7} ", "7 ");
        check("${8/**/;9} ", "9 ");
        check("${//8 } 9 \n 10 } 11 ", "10 11 ");
    }

    @Test
    public void tags() throws Exception
    {
        check("", "");
        check("<$$>", "");
        check(" { <$$> } ", " {  } ");
        check(" { <$var a;$> } ", " {  } ");
        check(" { <$var a;a=5;$> } ", " {  } ");
        check(" { <$var a;a=5;$>${a} } ", " { 5 } ");
        check(" {<$  for (var i=0; i<5; i++)  {  $> ${i}<$  }  $> } ", " { 0 1 2 3 4 } ");
        // escape
        check("<$>", "$");
        check("{$}", "$");
        check("${$}", "$$");
        check("<<$>>", "<$>");
        check("<<$>{$}{{$}}$<$>>", "<$${$}$$>");
    }

    @Test
    public void variable() throws Exception
    {
        TemplateManager.parse("${a=1} ${++a}").eval(new SimpleBindings(), result);
        assertEquals("1 2", result.toString());
    }

    @Test
    public void function() throws Exception
    {
        Bindings bindings = new SimpleBindings ();
        bindings.put("a", this);
        TemplateManager.parse("${a.func()}").eval(bindings, result);
        assertEquals(func(), result.toString());
    }
    public String func()  {  return "some value";  }

    @Test
    public void include() throws Exception
    {
        final HashMap<String, String> sources = new HashMap<> ();
        sources.put("/file1", "included source 1: ${a=1}");
        sources.put("/file2", "included source 2: ${a++;a++}");
        TemplateManager manager = new TemplateManager () {
            public Template getTemplate(String path) throws Exception  {  return parse(sources.get(path), this, path);  }
            public void putTemplate(String path, Template template)  {}
        };

        //    from parent
        result.clear();
        TemplateManager.parse("${a=1} ${include('/file2');}", manager, null)
                .eval(new SimpleBindings(), result);
        assertEquals("1 included source 2: 2", result.toString());

        //    from sibling
        result.clear();
        TemplateManager.parse("${include('/file1');}; ${include('/file2');}; ${a}", manager, null)
                .eval(new SimpleBindings(), result);
        assertEquals("included source 1: 1; included source 2: 2; 3", result.toString());

        //    relative
        result.clear();
        TemplateManager.parse("relative: ${include('file1');}", manager, "/").eval(new SimpleBindings(), result);
        assertEquals("relative: included source 1: 1", result.toString());

        //    parent and current paths
        result.clear();
        sources.put("/file1", "one");
        sources.put("/file2", "two");
        sources.put("/dir1/file1", "one.one");
        sources.put("/dir1/file2", "one.two");
        sources.put("/dir2/file1", "two.one");
        sources.put("/dir2/file2", "two.two");
        String source = "${include('file1');} ${include('./file2');} ${include('../file1');} ${include('/file2');} ${include('../dir2/file1');} ${include('/dir2/file2');}";
        TemplateManager.parse(source, manager, "/dir1/").eval(new SimpleBindings(), result);  // путой путь относительно каталога, т.к. он не важен
        assertEquals("one.one one.two one two two.one two.two", result.toString());

        result.clear();
        sources.put("/dir1/dir2/dir3/file3", "one.two.three.four");
        source = "${include('../../../file1');} ${include('../../file2');} ${include('file3');}";
        TemplateManager.parse(source, manager, "/dir1/dir2/dir3/?").eval(new SimpleBindings(), result);
        assertEquals("one one.two one.two.three.four", result.toString());
    }

    @Test
    public void childs1() throws Exception
    {
        String templateSource =
            "Some template. " +
            "<$ var part = :$>Child template part<$:;$>" +
            "<$ evaluate(part); $>";
        TemplateManager.parse(templateSource).eval(new SimpleBindings (), result);
        assertEquals("Some template. Child template part", result.toString());
    }

    @Test
    public void childs2() throws Exception
    {
        // Child variables can not touch parent variables.
        // This restrict derived from save Java Scripting API rule, that engine must not save changed bindings after eval
        // (it is Nashorn's behaviour, Rhino saves values)

        String templateSource =
            "Some template.\n" +
            "<$ var b = 2; $>" +
            "<$ var part = :$>Child template part (${a++}, ${b++})\n<$:;$>" +
            "<$ evaluate(part); $>";
        Bindings bindings = TemplateManager.createBindings();
        bindings.put("a", 1);
        TemplateManager.parse(templateSource).eval(bindings, result);
        assertEquals("Some template.\nChild template part (1, 2)\n", result.toString());
        result.clear();

        templateSource +=
            "<$ for (var i=0; i<3; i++) evaluate(part); $>";
        TemplateManager.parse(templateSource).eval(bindings, result);
        assertEquals(
                "Some template.\n" +
                "Child template part (1, 2)\n" +
                "Child template part (1, 2)\n" +
                "Child template part (1, 2)\n" +
                "Child template part (1, 2)\n",
                result.toString());
    }

    @Test
    public void childs3() throws Exception
    {
        String templateSource =
            "Head.\n" +
            "<$ var part1=:$>" +
                "Part A (<$ if (typeof a == 'undefined') a = 0; $>${++a})\n" +
            "<$:, part2=:$>" +
                "Part B (<$ if (typeof a == 'undefined') a = 0; $>${++a})\n" +
            "<$:$><$;$>" +
            "<$ evaluate(part1); evaluate(part2); evaluate(part1); evaluate(part1); evaluate(part2); $>";
        TemplateManager.parse(templateSource).eval(TemplateManager.createBindings(), result);
        assertEquals("Head.\nPart A (1)\nPart B (1)\nPart A (1)\nPart A (1)\nPart B (1)\n", result.toString());
    }
}
