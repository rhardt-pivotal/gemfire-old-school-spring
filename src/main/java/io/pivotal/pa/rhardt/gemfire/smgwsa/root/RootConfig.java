package io.pivotal.pa.rhardt.gemfire.smgwsa.root;

//import com.gemstone.gemfire.cache.Region;
//import com.gemstone.gemfire.cache.client.ClientCache;
//import com.gemstone.gemfire.cache.client.ClientCacheFactory;
//import com.gemstone.gemfire.cache.client.ClientRegionFactory;
//import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
//import com.gemstone.gemfire.management.internal.cli.converters.ConnectionEndpointConverter;
//import com.gemstone.gemfire.management.internal.cli.util.ConnectionEndpoint;
//import com.gemstone.gemfire.pdx.PdxInstance;
//import com.gemstone.gemfire.pdx.PdxSerializer;
//import com.gemstone.gemfire.pdx.ReflectionBasedAutoSerializer;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;


import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
//import org.apache.geode.management.internal.cli.converters.ConnectionEndpointConverter;
import org.apache.geode.internal.cache.GemFireCacheImpl;
import org.apache.geode.management.internal.cli.util.ConnectionEndpoint;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.PdxSerializer;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


@Configuration
public class RootConfig {

    @Value("#{environment['GEMFIRE_USERNAME'] ?: 'developer'}")
    private String username;

    @Value("#{environment['GEMFIRE_PASSWORD'] ?: 'ineedaraise'}")
    private String password;

    @Value("#{environment['GEMFIRE_LOCATORS'] ?: 'localhost[10334]'}")
    private String locators;

    @Value("#{environment['GEMFIRE_LOGLEVEL'] ?: 'INFO'}")
    private String logLevel;

    @Value("#{environment['GEMFIRE_APPNAME'] ?: 'GemfireDemo'}")
    private String appName;


    private static final String SECURITY_CLIENT = "security-client-auth-init";
	private static final String SECURITY_USERNAME = "security-username";
	private static final String SECURITY_PASSWORD = "security-password";


    @Bean
    Properties gemfireProperties() throws Exception{

        Properties gemfireProperties = new Properties();

        gemfireProperties.setProperty("name", appName);
        gemfireProperties.setProperty("log-level", logLevel);
        gemfireProperties.setProperty(SECURITY_CLIENT, "io.pivotal.pa.rhardt.gemfire.smgwsa.util.UserAuthInitialize.create");
        gemfireProperties.setProperty(SECURITY_USERNAME, username);
        gemfireProperties.setProperty(SECURITY_PASSWORD, password);

        return gemfireProperties;
    }

    @Bean(name="AlbumCache")
    ClientCache gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) throws Exception{
        try{
            return GemFireCacheImpl.getExisting();
        }
        catch (Exception ex){
            System.out.println("******** NO EXISTING ***********");
        }
        ClientCacheFactory ccf = new ClientCacheFactory(gemfireProperties)
                .setPdxSerializer(pdxSerializer())
                .setPdxReadSerialized(false);
        getLocators().forEach(ce -> ccf.addPoolLocator(ce.getHost(), ce.getPort()));

        return ccf.create();
    }

    private ConnectionEndpoint toConnectionEndpoint(String s) {
        return this.convertFromText(s);
    }

    private List<ConnectionEndpoint> getLocators() throws Exception{
        return Arrays.stream(locators.split(",")).map(String::trim)
                .map(this::toConnectionEndpoint)
                .collect(Collectors.toList());
    }

    @Bean(name = "Album_default")
    public Region<String, PdxInstance> albumRegion (ClientCache gemfireCache) {

        ClientRegionFactory<String, PdxInstance> albumRegion =
                gemfireCache.createClientRegionFactory(ClientRegionShortcut.PROXY);

        return albumRegion.create("Album_default");
    }

    PdxSerializer pdxSerializer() {

        PdxSerializer pdxSerializer = new ReflectionBasedAutoSerializer(".*");
        return pdxSerializer;
    }

    private ConnectionEndpoint convertFromText(String value) {
        String endpointStr = value.trim();
        String hostStr = "localhost";
        String portStr = "";
        int port = 1099;
        if (!endpointStr.isEmpty()) {
            int openIndex = endpointStr.indexOf("[");
            int closeIndex = endpointStr.indexOf("]");
            if (openIndex != -1) {
                if (closeIndex == -1) {
                    throw new IllegalArgumentException("Expected input: host[port] or host. Invalid value specified endpoints : " + value);
                }

                hostStr = endpointStr.substring(0, openIndex);
                portStr = endpointStr.substring(openIndex + 1, closeIndex);
                if (portStr.isEmpty()) {
                    throw new IllegalArgumentException("Expected input: host[port] or host. Invalid value specified endpoints : " + value);
                }

                try {
                    port = Integer.valueOf(portStr);
                } catch (NumberFormatException var11) {
                    throw new IllegalArgumentException("Expected input: host[port], Port should be a valid number between 1024-65536. Invalid value specified endpoints : " + value);
                }
            } else {
                if (closeIndex != -1) {
                    throw new IllegalArgumentException("Expected input: host[port] or host. Invalid value specified endpoints : " + value);
                }

                hostStr = endpointStr;
            }
        }

        return new ConnectionEndpoint(hostStr, port);
    }


}
