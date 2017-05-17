package ru.otus.chepiov.tfplugin;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import ru.otus.chepiov.tf.ReflectionUtils;
import ru.otus.chepiov.tf.Runner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Test plugin Mojo.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@SuppressWarnings("unused")
@Mojo(name = "run-tests", defaultPhase = LifecyclePhase.TEST)
public class TestMojo extends AbstractMojo {

    @Parameter(property = "run-tests.packageName")
    private String packageName;

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    @Parameter(property = "run-tests.classNames")
    private String[] classNames;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // creating class loader for testing code. Awful!!!
        final URLClassLoader loader;
        try {
            final List<String> classpathElements = project.getCompileClasspathElements();
            final List<URL> classPaths = new ArrayList<>();

            project.getCompileClasspathElements().forEach(element -> {
                try {
                    classPaths.add(new File(element).toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(element + "is invalid classpath element", e);
                }
            });
            loader = URLClassLoader.newInstance(classPaths.toArray(new URL[0]), getClass().getClassLoader());
        } catch (DependencyResolutionRequiredException | RuntimeException e) {
            throw new MojoExecutionException("Dependency resolution failed", e);
        }

        getLog().info("-------------------------------------------------------");
        getLog().info("OTUS TESTS");
        getLog().info("-------------------------------------------------------");
        getLog().info("");
        List<AssertionError> errors = new ArrayList<>();

        // direct classes
        if (Objects.nonNull(classNames)) {
            getLog().info("Class names: " + Arrays.toString(classNames));
            final List<Class<?>> suits = new ArrayList<>();
            for (String cl : classNames) {
                try {
                    suits.add(loader.loadClass(cl));
                } catch (ClassNotFoundException e) {
                    throw new MojoExecutionException("Illegal class name: " + cl);
                }
            }
            try {
                Runner.run(suits.toArray(new Class<?>[]{}));
            } catch (AssertionError err) {
                errors.add(err);
            }
        }

        // package
        if (Objects.nonNull(packageName)) {
            getLog().info("Package name: " + packageName);
            try {
                Class<?>[] classes = ReflectionUtils.findAllAnnotatedClasses(packageName, loader);
                getLog().info("Class names in package: " + Arrays.toString(classes));
                Runner.run(classes);
            } catch (AssertionError err) {
                errors.add(err);
            } catch (RuntimeException e) {
                throw new MojoExecutionException("Illegal package name: " + packageName, e);
            }
        }

        // result
        getLog().info("Results:");
        if (errors.isEmpty()) {
            getLog().info("All tests passed");
        } else {
            errors.forEach(err -> getLog().error(err.getMessage()));
            List<String> source = errors.stream().map(Throwable::getMessage).collect(Collectors.toList());
            throw new MojoFailureException(source, "Error during tests execution", "Error during tests execution");
        }
    }
}
