package ru.otus.chepiov.l9.test;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.model.Address;
import ru.otus.chepiov.db.model.Phone;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l11.SoftRefCacheEngine;
import ru.otus.chepiov.l12.AdminServlet;
import ru.otus.chepiov.l12.LoginFilter;
import ru.otus.chepiov.l12.LoginServlet;
import ru.otus.chepiov.l13.Helper;
import ru.otus.chepiov.l9.Executor;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.HashSet;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class RunWebServer {

    public static void main(String[] args) throws Exception {
        Helper.loadDriver();

        Helper.prepareDb();
        final Executor dbService = new Executor(
                Helper.H2_DRIVER,
                Helper.JDBC_H2_TEST_URL,
                new HashSet<Class<? extends DataSet>>() {{
                    add(User.class);
                    add(Address.class);
                    add(Phone.class);
                }},
                10, new SoftRefCacheEngine<>());

        final Server server = new Server();
        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(8090);
        server.setConnectors(new Connector[]{connector});

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        context.addServlet(new ServletHolder(new AdminServlet(dbService, null)), "/admin");
        context.addServlet(new ServletHolder(LoginServlet.class), "/");

        context.addFilter(LoginFilter.class, "/admin",
                EnumSet.of(DispatcherType.REQUEST));

        server.setHandler(context);

        server.start();
        System.err.println("Web server available at http://localhost:8090/");
        System.err.println("Press enter to stop");
        @SuppressWarnings("unused") final int read = System.in.read();
        server.stop();

        Helper.clearTables();
        Helper.clearDb();
    }


}
