package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.ComotContext;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author omoser
 */
@ContextConfiguration(classes = ComotContext.class)
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractSalsaClientTest {

    private final static String ENDPOINT_ADDRESS = "http://localhost";

    @Autowired
    private ApplicationContext context;

    private static Server server;

    protected static int port;

    @Before
    @Rollback(false)
    public void initialize() throws Exception {
        assertNotNull(context);
        if (server == null || !server.isStarted()) {
            startServer();
        }

        assertTrue(server.isStarted());
    }

    @AfterClass
    public static void teardown() throws Exception {
        server.destroy();
    }

    private void startServer() throws Exception {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(getServiceBeanClass());

        Object serviceBean = context.getBean(getServiceBeanName(), getServiceBeanClass());
        sf.setServiceBean(serviceBean);

        List<Object> providers = new ArrayList<>();
        providers.add(new JacksonJaxbJsonProvider());
        //providers.add(context.getBean(ArtifactExceptionMapper.class)); // todo add the exception mapper here
        sf.setProviders(providers);

        List<Feature> features = new ArrayList<>();
        features.add(new LoggingFeature());
        sf.setFeatures(features);

        sf.setResourceProvider(getServiceBeanClass(), new SingletonResourceProvider(serviceBean, true));
        int port = getAvailablePort();
        AbstractSalsaClientTest.port = port;

        sf.setAddress(ENDPOINT_ADDRESS + ":" + port + "/");
        server = sf.create();
    }

    protected abstract Class getServiceBeanClass();

    protected String getServiceBeanName() {
        return getServiceBeanClass().getSimpleName().toLowerCase();
    }

    protected int getAvailablePort() throws InitializationError {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException ignored) {
            throw new InitializationError("Cannot determine an usable free port");
        }
    }

}

