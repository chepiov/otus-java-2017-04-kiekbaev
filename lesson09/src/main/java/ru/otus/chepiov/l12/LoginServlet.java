package ru.otus.chepiov.l12;

import de.neuland.jade4j.template.JadeTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ru.otus.chepiov.l12.TemplateHelper.CONFIG;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class LoginServlet extends HttpServlet {

    private static Map<String, String> REPOSITORY = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("admin", "admin");
    }});

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (Objects.isNull(req.getSession().getAttribute("login"))) {
            final String login = req.getParameter("login");
            final String rightPass = REPOSITORY.get(login);
            if (Objects.nonNull(login) && Objects.nonNull(rightPass) && Objects.equals(rightPass, req.getParameter("password"))) {
                req.getSession().setAttribute("login", login);
                redirect(resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(getContent("Illegal login or password"));
            }
        } else {
            redirect(resp);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        if (Objects.isNull(req.getSession().getAttribute("login"))) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(getContent(""));
        } else {
            redirect(resp);
        }
    }

    private void redirect(HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("admin");
    }

    private String getContent(final String message) throws IOException {
        final JadeTemplate template = CONFIG.getTemplate("public/login.jade");
        final Map<String, Object> model = new HashMap<>();
        model.put("message", message);
        return CONFIG.renderTemplate(template, model);
    }
}
