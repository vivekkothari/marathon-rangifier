package com.vivek.marathon.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivek.marathon.agent.model.AgentConfig;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Agent {

	private static final String MARATHON_ENDPOINT = "MARATHON_ENDPOINT";

	private static final String SERVICE_NAMESPACE = "SERVICE_NAMESPACE";

	private static final String SERVICE_ENV = "SERVICE_ENV";

	private static final String ZK_CONNECTION_STRING = "ZK_CONNECTION_STRING";

	private static final String REFRESH_INTERVAL = "REFRESH_INTERVAL";

	public static void main(String[] args) throws Exception {
		Map<String, String> env = System.getenv();
		AgentConfig agentConfig = AgentConfig.builder()
											 .marathonEndpoint(env.get(MARATHON_ENDPOINT))
											 .namespace(env.getOrDefault(SERVICE_NAMESPACE, "olacabs"))
											 .environment(env.get(SERVICE_ENV))
											 .zkConnectionString(env.get(ZK_CONNECTION_STRING))
											 .refreshInterval(Integer.valueOf(env.getOrDefault(REFRESH_INTERVAL, "10")))
											 .build();

		log.info("agent config received is: " + agentConfig);

		Marathon marathon = MarathonClient.getInstance(agentConfig.getMarathonEndpoint());
		MarathonServiceClient marathonServiceClient = new MarathonServiceClient(marathon);
		ServiceProviderBuilder serviceProviderBuilder = new ServiceProviderBuilder(agentConfig, new ObjectMapper());
		ManagedServiceRegistry serviceRegistry = new ManagedServiceRegistry(marathonServiceClient,
																			serviceProviderBuilder);

		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

		log.info("starting the service");

		executorService.scheduleAtFixedRate(serviceRegistry::start, 0, agentConfig.getRefreshInterval(),
											TimeUnit.SECONDS);

		Runtime.getRuntime()
			   .addShutdownHook(new Thread(() -> {
				   serviceRegistry.stop();
				   executorService.shutdown();
			   }));

	}
}
