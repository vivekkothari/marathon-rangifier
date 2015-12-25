package com.vivek.marathon.agent;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.vivek.marathon.agent.model.ServiceInstance;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.LabelSelectorQuery;
import mesosphere.marathon.client.model.v2.Task;

import java.util.*;

import static mesosphere.marathon.client.model.v2.LabelSelectorQuery.QueryElement;

/**
 * Created by vivek.kothari on 17/12/15.
 */
@RequiredArgsConstructor
@Slf4j
public class MarathonServiceClient {
	private static final String ZK_SERVICE_NAME = "ZK_SERVICE_NAME";
	private static final Splitter COLON_SPLITTER = Splitter.on(':').trimResults().omitEmptyStrings();
	private final Marathon marathon;

	@SneakyThrows
	public Set<ServiceInstance> getInstances() {
		Set<ServiceInstance> serviceInstances = new HashSet<>();
		QueryElement queryElement = QueryElement.builder()
												.lhs(ZK_SERVICE_NAME)
												.operator(LabelSelectorQuery.QueryOperator.EXISTS)
												.build();
		LabelSelectorQuery labelSelectorQuery = LabelSelectorQuery.builder()
																  .addQueryElement(queryElement).build();

		List<App> apps = marathon.getApps(labelSelectorQuery.getQuery()).getApps();
		log.info("apps received from Marathon are: " + apps);
		apps.forEach(app -> {
			Map<String, String> label = app.getLabels();
			String serviceName = label.get(ZK_SERVICE_NAME);
			List<String> list = COLON_SPLITTER.splitToList(serviceName);
			int portIndex = list.size() == 1 ? 0 : Integer.valueOf(list.get(1));
			Collection<Task> tasks = app.getTasks();
			tasks = tasks == null ? marathon.getAppTasks(app.getId()).getTasks() : tasks;
			tasks.forEach(task -> {
				List<Integer> ports = Lists.newArrayList(task.getPorts());
				ServiceInstance serviceInstance = new ServiceInstance(task.getHost(), ports.get(portIndex),
																	  list.get(0));
				serviceInstances.add(serviceInstance);
			});
		});
		return serviceInstances;
	}

}
