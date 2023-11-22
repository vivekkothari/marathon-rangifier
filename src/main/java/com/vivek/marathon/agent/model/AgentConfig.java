package com.vivek.marathon.agent.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/** Created by vivek.kothari */
public class AgentConfig {

  private String marathonEndpoint;

  private String zkConnectionString;

  private String namespace = "olacabs";

  private String environment;

  private int refreshInterval = 5;
}
