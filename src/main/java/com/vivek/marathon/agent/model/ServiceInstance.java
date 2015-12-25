package com.vivek.marathon.agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by vivek.kothari on 17/12/15.
 */
@Data
@AllArgsConstructor
public class ServiceInstance {
    private String hostName;
    private int port;
    private String serviceName;
}
