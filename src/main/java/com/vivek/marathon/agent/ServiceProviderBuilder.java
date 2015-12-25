package com.vivek.marathon.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceProviderBuilders;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.vivek.marathon.agent.model.AgentConfig;
import com.vivek.marathon.agent.model.ServiceInstance;
import com.vivek.marathon.agent.model.ShardInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by vivek.kothari on 17/12/15.
 */
@AllArgsConstructor
@Slf4j
public class ServiceProviderBuilder {

    private final AgentConfig agentConfig;
    private final ObjectMapper mapper;

    public ServiceProvider<ShardInfo> buildServiceProvider(ServiceInstance serviceInstance) {
        log.info("Building serviceProvider for " + serviceInstance);
        return ServiceProviderBuilders.<ShardInfo>shardedServiceProviderBuilder()
                .withConnectionString(agentConfig.getZkConnectionString())
                .withNamespace(agentConfig.getNamespace())
                .withServiceName(serviceInstance.getServiceName())
                .withSerializer(data -> {
                    try {
                        return mapper.writeValueAsBytes(data);
                    } catch (final JsonProcessingException ignore) {
                        log.error("Error - ", ignore);
                    }
                    return null;
                })
                .withHostname(serviceInstance.getHostName())
                .withPort(serviceInstance.getPort())
                .withNodeData(new ShardInfo(agentConfig.getEnvironment()))
                .withHealthcheck(() -> HealthcheckStatus.healthy)
                .buildServiceDiscovery();
    }
}
