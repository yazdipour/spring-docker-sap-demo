package com.example.demo;

import org.junit.jupiter.api.Test;
import java.io.IOException;

class DockerAPIUtilTest {
    @Test
    void getDockerClient() throws IOException {
        var dockerClient= new DockerService().getDockerClient();
        dockerClient.pingCmd().exec();
    }
}