import ru.and390.template.TemplateManager;
import ru.and390.template.WatchFileTemplateManager;
import ru.and390.utils.StringList;

import javax.script.Bindings;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * And390 - 23.04.2015
 */
@WebFilter("/*")
public class ExampleFilter implements Filter
{
    protected ServletContext servletContext;
    protected String encoding;
    protected WatchFileTemplateManager templateManager;

    @Override
    public void init(FilterConfig config) throws ServletException
    {
        servletContext = config.getServletContext();
        encoding = config.getInitParameter("encoding");
        if (encoding==null)  encoding = "UTF-8";
        try  {  templateManager = new WatchFileTemplateManager(new File(config.getServletContext().getRealPath("")), encoding);  }
        catch (IOException e)  {  throw new ServletException (e);  }
    }

    @Override
    public void destroy()
    {
        try  {
            templateManager.close();
            TemplateManager.free();
        }
        catch (IOException|InterruptedException e)  {  throw new RuntimeException (e);  }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String context = request.getContextPath();
        String path = request.getRequestURI().substring(context.length());
        path = java.net.URLDecoder.decode(path, "UTF-8");
        if (!path.startsWith("/"))  path = '/'+path;
        //    try process
        try  {
            Bindings bindings = processRequest(request, response, path);
            if (bindings!=null)  {
                //    execute template, save response to buffer
                StringList buffer = new StringList ();
                if (templateManager.evalIfExists(path, bindings, buffer))  {
                    //    write response
                    for (String s : buffer)  response.getWriter().write(s);
                    return;
                }
                // else template is not found, default process causes 404 (if no one will process request)
            }
        }
        catch (IOException|ServletException|RuntimeException e)  {  throw e;  }
        catch (Exception e)  {  throw new ServletException (e);  }
        //    default process if function returns false
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public Bindings processRequest(HttpServletRequest request, HttpServletResponse response, String path) throws Exception
    {
        if (!path.endsWith(".html"))  return null;

        Bindings bindings = TemplateManager.createBindings();

        //    pass all request parameters to template and some default variables
        for (Object name : request.getParameterMap().keySet())  {
            String[] values = request.getParameterValues((String) name);
            bindings.put((String)name, values.length==1 ? values[0] : values);
        }
        bindings.put("request", request);
        bindings.put("response", response);
        bindings.put("contextPath", request.getContextPath());

        //
        User user = (User) request.getSession().getAttribute("user");
        String error = null;
        try
        {
            //    logout action
            if (request.getParameter("logout")!=null)  {
                user = null;
                request.getSession().removeAttribute("user");
            }
            //    process login action
            String login = request.getParameter("login");
            if (login!=null)  {
                if (login.equals(""))  throw new ClientException ("Empty parameter 'login'");
                user = new User (login);
                request.getSession().setAttribute("user", user);
            }
            //    set user IP
            if (user!=null)  {
                user.ip = request.getRemoteAddr();
            }
        }
        catch (ClientException e)
        {
            error = e.getMessage();
        }

        bindings.put("user", user);
        bindings.put("error", error);
        return bindings;
    }

    public static class User
    {
        public final String name;
        private String ip;
        public User(String name_)  {  name = name_;  }
        // you can use properties or public fields
        public String getIp()  {  return ip;  }
        public void setIp(String ip)  {  this.ip = ip;  }
    }

    /**
     * Ќеправильные действи€ со стороны клиента.
     */
    public static class ClientException extends Exception
    {
        public ClientException(String message)  {  super(message);  }
    }
}
