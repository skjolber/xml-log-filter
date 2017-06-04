package com.github.skjolber.xmlns.schema.logger;


import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilter;


/**
 * Jersey JAXB example application.
 *
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/logger/");

    public static void main(String[] args) {
        try {
            System.out.println("Example logger App");
            
            ResourceConfig config;
            if(args == null || args.length == 0) {
            	config = createApp1();
            } else if(args[0].equals("1")) {
            	config = createApp1();
            } else if(args[0].equals("2")) {
            	config = createApp2();
        	} else {
        		throw new IllegalArgumentException();
        	}

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, config);

            System.out.println(
                    String.format("Application started.%nTry out %s%nHit enter to stop it...", BASE_URI));
            System.in.read();
            server.shutdownNow();
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ResourceConfig createApp1() {
        final ResourceConfig rc = new ResourceConfig()
                .packages(
                		LoggerResource1.class.getPackage().getName(), XmlLogFilter.class.getPackage().getName());

        
        return rc;
    }
    
    public static ResourceConfig createApp2() {
        final ResourceConfig rc = new ResourceConfig()
                .packages(
                		LoggerResource2.class.getPackage().getName(), XmlLogFilter.class.getPackage().getName());

        
        return rc;
    }
}
