package com.example.demo;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/***
 * https://github.com/docker-java/docker-java/blob/master/docs/getting_started.md
 * https://www.baeldung.com/docker-java-api
 */
@Component
public class DockerService {
    public Logger logger = LoggerFactory.getLogger(DockerService.class);
    private final DockerClient dockerClient;

    @Autowired
    public DockerService() throws IOException {
        this.dockerClient = buildDockerClient();
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    /***
     *  By default, the Docker daemon can only be accessible at the unix:///var/run/docker.sock file.
     *  We can locally communicate with the Docker engine listening on the Unix socket unless otherwise configured.
     * @return DockerClient is where we can establish a connection between a Docker engine/daemon and our application.
     */
    protected DockerClient buildDockerClient() throws IOException {
        if (dockerClient != null) return dockerClient;
        Properties properties = new Properties();
        properties.load(DockerService.class.getClassLoader().getResourceAsStream("docker-java.properties"));
        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withProperties(properties)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        logger.info("DockerClientImpl.getInstance() for the first time");
        return DockerClientImpl.getInstance(config, httpClient);
    }
}
