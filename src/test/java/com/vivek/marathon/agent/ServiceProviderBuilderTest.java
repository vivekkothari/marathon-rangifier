package com.vivek.marathon.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.model.ServiceNode;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.vivek.marathon.agent.model.AgentConfig;
import com.vivek.marathon.agent.model.ServiceInstance;
import com.vivek.marathon.agent.model.ShardInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by vivek.kothari on 25/12/15.
 */
public class ServiceProviderBuilderTest {

    private ServiceProviderBuilder builder;

    @Before
    public void setup() {
        ObjectMapper mapper = new ObjectMapper();
        AgentConfig config = AgentConfig.builder()
                .environment("stage")
                .marathonEndpoint("some.url")
                .namespace("namespace")
                .refreshInterval(10)
                .zkConnectionString("zk.com")
                .build();
        builder = new ServiceProviderBuilder(config, mapper);
    }

    @Test
    public void testBuildServiceProvider() throws Exception {
        ServiceInstance serviceInstance = new ServiceInstance("service.host.name", 8080, "service_name");
        ServiceProvider<ShardInfo> provider = builder.buildServiceProvider(serviceInstance);
        Assert.assertNotNull(provider);
        ServiceNode<ShardInfo> serviceNode = provider.getServiceNode();
        Assert.assertNotNull(serviceNode);
    }
}