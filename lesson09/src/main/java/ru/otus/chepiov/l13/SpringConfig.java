package ru.otus.chepiov.l13;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.model.Address;
import ru.otus.chepiov.db.model.Phone;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l11.CacheEngine;
import ru.otus.chepiov.l11.SoftRefCacheEngine;
import ru.otus.chepiov.l9.Executor;

import java.util.HashSet;

@Configuration
public class SpringConfig {

    @Bean
    public CacheEngine<Long, User> getCacheEngine() {
        return new SoftRefCacheEngine<>();
    }

    @Bean
    public Executor getExecutor(final CacheEngine<Long, User> cacheEngine) {
        return new Executor(
                Helper.H2_DRIVER,
                Helper.JDBC_H2_TEST_URL,
                new HashSet<Class<? extends DataSet>>() {{
                    add(User.class);
                    add(Address.class);
                    add(Phone.class);
                }},
                10,
                cacheEngine);
    }
}
