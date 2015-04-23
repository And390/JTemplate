JTemplate
=========
Is a simple Java template engine uses JavaScript via Java Scripting API. It allows you to use all JavaScript features including conditions, loops and functions.

## Usage
The simples usage is:
```java
    Bindings values = TemplateManager.createBindings();
    values.put("name", "World");
    TemplateManager.parse("Hello ${name}!").eval(values, System.out);
```
`TemplateManager.parse()` transforms a content to `Template` object that has method `eval` that can execute template using `Bindings` and writing result to `Appendable`. JTemplate is a rather thin transparent layer under Java Scripting API. `Template` implementation can be a wrap for `CompiledScript` if it supports by the script engine realisation (it supports by both Rhino for Java 7 and Nashorn for Java 8). `Bindings` is an object from Java Scripting.
Most likely you will need a `TemplateManager` instance for templates including. `CacheTemplateManager` is thread-safe caching realisation. It's subclasses, `FileTemplateManager` loads template contents from files from specified directory and `WatchFileTemplateManager` additionally monitors that directory and unloads changed files.
```java
FileTemplateManager manager =  new FileTemplateManager ();
Bindings values = TemplateManager.createBindings();
manager.eval("/dir/path", values, output);
```
For more examples see `test` directory and example web application in `testweb`.

## Syntax
There are two common syntax constructions, `${}` can contains expression that uses to insert value (like JSP expressions `<%=...%>`):
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
`${}` also can contain many expressions and also can work like `<$$>` if it ends with `;` (but obviosly it can't contain `}`):
```text
    ${ a += b; c += a} - only prints last expression, i.e. value of c
    ${ c++; } - nothing to print
```
To escape the dollar sign you can use `<$>` or `{$}`. There are no special syntax for comments, but you can use JavaScript comments inside scripts any way you want.
```text
    <$ 
        a = b + c;  /* block comment, brackets $> can be here */ 
        i++;  //line comment
    $>
```
To include one template into another use javascript template function `include` (which is wrap for the same `context.include`):
```text
    <$ include('/dir/path'); $>  with absolute
    <$ include('../path'); $>  or relative paths
```
To directly print values from scripts you can use `output` function (which also wraps `context.output`):
```text
    <$ for (var i=0; i<5; i++) output(' '+i); /*prints " 1 2 3 4 5"*/$>
```
Of course you can define your own functions and objects.
```text
    <$
        function replace(string, map) {
	    for (var i in map)  string = string.replace(i, map[i]);
            return string;
        }
    $>
    Nobody expects the ${replace('Spanish Inquisition', {'S': '$', 'a': '@'})}
```
Additionally, it is one powerfull feature, **child templates**. You can define a subtemplate inside base template (and do it recursively in subtemplate) and use it multiple times wherever you want.
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
Comma-syntax just creates a `Template` instance. Here, it assings to `part1` but you can pass it anywhere in the script. `evaluate` function is a simple wrap for `part1.eval(context.bindings, context.out)`.