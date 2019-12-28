package org.ka.jenkins.masterless.image.junkins;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.jvnet.hudson.test.NoListenerConfiguration;
import org.jvnet.hudson.test.ThreadPoolImpl;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Jetty {
    private static final Logger LOG = Logger.getLogger(Jetty.class.getName());

    public interface JettyContext {
        int getPort();
        ServletContext getServletContext();
        Runnable doStop();
    }

    public static JettyContext startNewServer(File warExploded) {
        LOG.entering(Jetty.class.getName(), "newContext");
        var server = new Server(jettyThreadPool());
        var webApp = newWebApp(warExploded);

        server.setHandler(webApp);

        var connector = new ServerConnector(server, 1, 1);
        var config = connector.getConnectionFactory(HttpConnectionFactory.class).getHttpConfiguration();
        config.setRequestHeaderSize(12 * 1024);
        connector.setHost("localhost");

        server.addConnector(connector);

        try {
            LOG.log(Level.FINE, "starting jetty");
            server.start();
            LOG.log(Level.FINE, "jetty started");
        } catch (Exception e) {
            throw new JunkinsException(e);
        }

        LOG.exiting(Jetty.class.getName(), "newContext");
        return new JettyContext() {

            @Override
            public int getPort() {
                return connector.getLocalPort();
            }

            @Override
            public ServletContext getServletContext() {
                return webApp.getServletContext();
            }

            @Override
            public Runnable doStop() {
                return () -> {
                    try {
                        synchronized (server) {
                            server.stop();
                        }
                    } catch (Exception e) {
                        throw new JunkinsException(e);
                    }
                };
            }
        };
    }

    private static WebAppContext newWebApp(File warExploded) {
        Logger.getLogger("org.eclipse.jetty").setLevel(Level.WARNING);

        var path = warExploded.getAbsolutePath();

        WebAppContext context = new WebAppContext(path, "/jenkins");
        context.setClassLoader(Jetty.class.getClassLoader());
        context.setConfigurations(new Configuration[]{new WebXmlConfiguration()});
        context.addBean(new NoListenerConfiguration(context));
        context.setMimeTypes(mimeTypes());
        context.getSecurityHandler().setLoginService(loginService());
        context.setResourceBase(path);

        return context;
    }

    private static LoginService loginService() {
        var service = new HashLoginService();
        service.setName("default");
        service.setUserStore(new UserStore());

        return service;
    }

    private static MimeTypes mimeTypes() {
        var mimeTypes = new MimeTypes();
        mimeTypes.addMimeMapping("js", "application/javascript");
        return mimeTypes;
    }

    private static ThreadPool jettyThreadPool() {
        return new ThreadPoolImpl(new ThreadPoolExecutor(1, 2, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("Jetty Thread Pool");
                    return t;
                }));
    }

    private Jetty() {}
}
