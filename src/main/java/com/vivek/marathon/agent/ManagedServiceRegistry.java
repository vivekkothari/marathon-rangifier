package com.vivek.marathon.agent;

import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.vivek.marathon.agent.model.ServiceInstance;
import com.vivek.marathon.agent.model.ShardInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class ManagedServiceRegistry {

	private final MarathonServiceClient marathonServiceClient;

	private final ServiceProviderBuilder serviceProviderBuilder;

	private ConcurrentMap<ServiceInstance, ServiceProvider<ShardInfo>> instances = new ConcurrentHashMap<>();

	public void start() {
		Set<ServiceInstance> serviceInstances = marathonServiceClient.getInstances();
		Predicate<? super ServiceInstance> staleInstancePredicate = serviceInstance -> !serviceInstances.contains(
				serviceInstance);
		instances.entrySet()
				 .stream()
				 .forEach(entry -> {
					 if (staleInstancePredicate.test(entry.getKey())) {
						 log.info("stopping old instance {}", entry.getKey());
						 this.stop(entry.getValue());
					 }
				 });
		instances.keySet().removeIf(staleInstancePredicate);
		serviceInstances.removeAll(instances.keySet());
		Map<ServiceInstance, ServiceProvider<ShardInfo>> serviceProviderMap = serviceInstances.stream()
																							  .collect(Collectors.toMap(Function.identity(),
																														serviceProviderBuilder::buildServiceProvider));
		serviceProviderMap.values().forEach(provider -> {
			try {
				log.info("starting service " + provider.getServiceNode());
				provider.start();
			} catch (Exception e) {
				log.error("error starting");
			}
		});
		instances.putAll(serviceProviderMap);
	}

	public void stop() {
		instances.values().forEach(this::stop);
	}

	private void stop(ServiceProvider<ShardInfo> provider) {
		try {
			provider.stop();
		} catch (Exception e) {
			log.error("error stopping");
		}
	}

}