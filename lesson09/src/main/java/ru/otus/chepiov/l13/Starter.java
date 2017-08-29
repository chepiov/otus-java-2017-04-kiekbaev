package ru.otus.chepiov.l13;

import liquibase.exception.LiquibaseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.otus.chepiov.l12.AdminServlet;
import ru.otus.chepiov.l12.LoginFilter;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.EnumSet;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */

@WebListener
public class Starter implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        try {
            Helper.loadDriver();
            Helper.prepareDb();
        } catch (ClassNotFoundException | LiquibaseException | SQLException e) {
            throw new RuntimeException(e);
        }
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(SpringConfig.class);
        ctx.refresh();

        final AdminServlet adminServlet = new AdminServlet(null, ctx);
        final ServletRegistration.Dynamic admin = sce.getServletContext().addServlet("admin", adminServlet);
        admin.addMapping("/admin");
        admin.setAsyncSupported(true);


        final LoginFilter loginFilter = new LoginFilter();
        final FilterRegistration.Dynamic filter = sce.getServletContext().addFilter("loginFilter", loginFilter);
        filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/admin");
        filter.setAsyncSupported(true);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        try {
            Helper.clearTables();
            Helper.clearDb();
        } catch (SQLException | LiquibaseException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
