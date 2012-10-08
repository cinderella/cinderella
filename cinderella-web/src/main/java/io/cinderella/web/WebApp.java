package io.cinderella.web;

import io.cinderella.CinderellaConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


public class WebApp {

    public static void main(String[] args) throws Exception {

        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(CinderellaConfig.class, WebConfig.class);
//        applicationContext.refresh();

        final ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(applicationContext));
        final ServletContextHandler context = new ServletContextHandler();

        context.setErrorHandler(null); // use Spring exception handler(s)
        context.setContextPath("/");
        context.addServlet(servletHolder, "/");

        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        final Server server = new Server(Integer.valueOf(webPort));

        server.setHandler(context);

        server.start();
        server.join();
    }

}
