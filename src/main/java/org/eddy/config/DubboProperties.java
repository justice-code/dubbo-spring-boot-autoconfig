package org.eddy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(DubboProperties.DUBBO_PROPERTIES)
public class DubboProperties {

    public static final String DUBBO_PROPERTIES = "dubbo";

    /**
     * dubbo application name
     */
    private String appName;
    /**
     * dubbo registry address
     */
    private String registry;
    /**
     * communication protocol, default is dubbo
     */
    private String protocol = "dubbo";
    /**
     * dubbo listen port, default 20800
     */
    private int port = 20800;
    /**
     * dubbo thread count, default 200
     */
    private int threads = 200;

    /**
     * dubbo version, may override by {@link com.alibaba.dubbo.config.annotation.Service#version()}
     */
    private String version = "";

    /**
     * dubbo group, may override by {@link com.alibaba.dubbo.config.annotation.Service#group()}
     */
    private String group = "";

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
