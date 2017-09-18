package cd.connect.samples.slackapp.servlet;

import cd.connect.spring.jersey.JerseyApplication;
import cd.connect.spring.jersey.JerseyApplicationBase;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;
import java.util.ServiceLoader;

abstract public class JerseyModule extends cd.connect.spring.jersey.JerseyModule {
    public static final String REST_SERVICES_URL_PATTERN = "/data/*";
    public static final String REST_SERVICES_ADMIN_URL_PATTERN = "/admin/data/*";




    @Override
    protected void postProcess(ServletContext servletContext, ApplicationContext ctx) {
        JerseyApplication jerseyResourceConfig = null;

        for(JerseyApplication app : ServiceLoader.load(JerseyApplication.class)) {
            if (jerseyResourceConfig != null) {
                throw new RuntimeException("Too many possible JerseyApplications, please include the dependency of one, not > 1.");
            } else if (!(app instanceof ResourceConfig)) {
                throw new RuntimeException(String.format("JerseyApplication `{}` is not an instance of ResourceConfig.", app.getClass().getName()));
            } else {
                jerseyResourceConfig = app;
            }
        }

        if (jerseyResourceConfig == null) {
            jerseyResourceConfig = new JerseyApplicationBase();
        }

        jerseyResourceConfig.init(ctx, registerResources());

        ResourceConfig base = ResourceConfig.class.cast(jerseyResourceConfig);

        servlet(new ServletContainer(base), s ->
                s.priority(100).name(this.getClass().getSimpleName() + "_servlet")
                        .url(getUrlServletPrefix())
                        .param(ServletProperties.FILTER_CONTEXT_PATH, getUrlServletPrefix()));
    }

    abstract protected String getUrlNoCachePattern();

    abstract protected String getUrlServletPrefix();
}
