JTemplate
=========
JTemplate is a simple template engine for Java with JavaScript expressions via Java Scripting API.
It allows you to use all JavaScript features including conditions, loops and functions.

## Usage
The simplest usage is:
```java
    Bindings values = TemplateManager.createBindings();
    values.put("name", "World");
    TemplateManager.parse("Hello ${name}!").eval(values, System.out);
```
`TemplateManager.parse()` transforms a content into `Template` object which you can then execute using `eval` method and `Bindings` object and write result to `Appendable`.
JTemplate is a rather thin transparent layer above Java Scripting API.
`Template` implementation can be a wrapper for `CompiledScript` if it's supported by the script engine implementation (it's supported by both Rhino for Java 7 and Nashorn for Java 8).
`Bindings` is an object from Java Scripting API.
Most likely you will need a `TemplateManager` instance to support `include` operation.
`CacheTemplateManager` is thread-safe caching implementation. 
It's subclasses, `FileTemplateManager` loads template contents from files from specified directory and `WatchFileTemplateManager` additionally monitors that directory and unloads changed files from cache.
```java
FileTemplateManager manager = new FileTemplateManager ();
Bindings values = TemplateManager.createBindings();
manager.eval("/dir/path", values, output);
```
For more examples see `test` directory and example web application in `testweb`.

## Syntax
There are two common syntax constructions, `${}` can contain expression that returns a value into template output (like JSP expressions `<%=...%>`):
```
    Total: ${count}.
```
and `<$$>` contains any script (like JSP scriptlets `<%...%>`):
```text
    <$ var items = loadItems(); $>
```
Another example:
```text
    <$ for (var i=0; i<items.length; i++) { $>
        <tr<$ if (items[i].error) { $> class="error"<$ } $>>  <td>${items[i].name}</td>  <td>${items[i].value}</td>  </tr>
    <$ } $>
```
`${}` also can contain many expressions and can work like `<$$>` if it ends with `;` or `}`. It also can contain curly brackets, but it can't contain not closed brackets `{` (in contrast to `<$$>` like in the above example):
```text
    ${ b += a; c += b } - only prints last expression, i.e. a value of c
    ${ c++; } - nothing to print because it ends with ';'
    ${ while (c<5) { c++; } } - nothing to print because it ends with '}'
    ${ a = { toString: function() { return "hello"; } }; a } - prints "hello"
```
Of course you can define your own functions and objects in scripts.
```text
    <$
        function replace(string, map) {
            for (var i in map)  string = string.replace(i, map[i]);
            return string;
        }
    $>
    Nobody expects the ${replace('Spanish Inquisition', {'S': '$', 'a': '@'})}
```
To escape the dollar sign you can use `<$>` or `{$}`. There is no special syntax for comments, but you can use JavaScript comments inside scripts any way you want.
```text
    <$ 
        /* block comment, closing brackets $> can be here */
        //line comment, closing brackets $> can be here
    $>
```
To include one template into another use javascript template function `include`:
```text
    <$ include('/dir/path'); $>  with absolute
    <$ include('../path'); $>  or relative paths
```
To directly print values from scripts you can use `output` function
```text
    <$ for (var i=0; i<5; i++) output(' '+i); /*prints " 1 2 3 4 5"*/$>
```
There is also a `context` object provided by Java Scripting engines. It's extended by template engine with some additional properties:

| js object | description |
| --------- | ----------- |
| `context.path`| path to template (passed from Java, can be null) |
| `context.manager` | `TemplateManager` instance (also can be null) |
| `context.bindings` | bindings passed from Java |
| `context.out` | instance of Java `Appendable` represents a template result output |
| `context.include` | global `include` function just wraps it |
| `context.output` | global `output` function just wraps it |

Additionally, there is one powerfull feature, **child templates**. You can define a subtemplate inside a base template (and do it recursively in subtemplate) and use it multiple times wherever you want.
```text
    <$ var part1 = :$>
        <div class="${item.class}">${item.text}</div>
    <$: ; $>
    <$ var item = items[0]; $>
    First item: ${evaluate(item);}
    <$ var item = items[1]; $>
    Second item: ${evaluate(item);}
    <$ for (var i=2; i<items.length; i++)  evaluate(items[i]);
```
Colon-syntax just creates a `Template` instance. Here, it assings to `part1` but you can pass it anywhere in the script.
`evaluate` function is a simple wrapper for `part1.eval(context.bindings, context.out)`.