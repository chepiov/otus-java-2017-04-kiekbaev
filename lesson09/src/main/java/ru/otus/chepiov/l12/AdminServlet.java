package ru.otus.chepiov.l12;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.FileTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import ru.otus.chepiov.db.model.Address;
import ru.otus.chepiov.db.model.Phone;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l9.Executor;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import static ru.otus.chepiov.l12.TemplateHelper.CONFIG;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class AdminServlet extends HttpServlet {

    private final Executor executor;


    public AdminServlet(final Executor executor) {
        this.executor = executor;
    }

    @Override
    protected void doGet(
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final String action = request.getParameter("action");
        if ("clear".equals(action)) {
            executor.getCache().dispose();
        } else if ("add".equals(action)) {
            executor.save(createRandomUser());
        } else if ("reload".equals(action)) {
            executor.load(Long.parseLong(request.getParameter("id")));
        }

        final List<User> users = executor.loadAll();
        final Map<String, Object> model = new HashMap<>();
        model.put("users", users);
        model.put("hitCount", executor.getCache().getHitCount());
        model.put("missCount", executor.getCache().getMissCount());

        final JadeTemplate template = CONFIG.getTemplate("public/index.jade");
        final String html = CONFIG.renderTemplate(template, model);

        final ByteBuffer content = ByteBuffer.wrap(html.getBytes());

        final AsyncContext async = request.startAsync();
        final ServletOutputStream out = response.getOutputStream();
        out.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                while (out.isReady()) {
                    if (!content.hasRemaining()) {
                        response.setStatus(200);
                        response.setContentType("text/html");
                        async.complete();
                        return;
                    }
                    out.write(content.get());
                }
            }

            @Override
            public void onError(Throwable t) {
                getServletContext().log("Async Error", t);
                async.complete();
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private static final Random RANDOM = new Random();

    private static User createRandomUser() {
        final User ironMan = new User();
        ironMan.setName(UUID.randomUUID().toString().substring(0, 10));
        ironMan.setAge(RANDOM.nextInt(100));
        final Address ironAddress = new Address();
        ironAddress.setStreet(UUID.randomUUID().toString().substring(0, 10));
        ironAddress.setIndex(100000 + RANDOM.nextInt(10000));
        ironMan.setAddress(ironAddress);
        final List<Phone> ironPhones = new ArrayList<Phone>() {{
            final Phone ironPhone = new Phone();
            ironPhone.setCode(1000 + RANDOM.nextInt(1000));
            ironPhone.setNumber(UUID.randomUUID().toString().substring(0, 10));
            add(ironPhone);
        }};
        ironMan.setPhones(ironPhones);
        return ironMan;
    }
}
