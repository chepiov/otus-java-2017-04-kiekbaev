package ru.otus.chepiov.l12;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.FileTemplateLoader;

import java.io.*;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
class TemplateHelper {

    static final JadeConfiguration CONFIG = new JadeConfiguration();

    static {
        CONFIG.setTemplateLoader(
                new FileTemplateLoader("", "UTF-8") {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public Reader getReader(String name) throws IOException {
                        final File source =
                                new File(this.getClass().getClassLoader().getResource(name).getFile());
                        return new InputStreamReader(new FileInputStream(source), "UTF-8");
                    }
                });
    }

    private TemplateHelper() {
    }
}
