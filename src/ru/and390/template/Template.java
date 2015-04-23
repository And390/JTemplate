package ru.and390.template;

import javax.script.Bindings;

/**
 * And390 - 30.11.13
 */
public interface Template
{
    public void eval(Bindings bindings, Appendable output) throws Exception;
}
