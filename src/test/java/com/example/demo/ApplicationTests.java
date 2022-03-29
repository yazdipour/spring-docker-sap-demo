package com.example.demo;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class ApplicationTests {
    @Test
    void pingTheDockerDaemon() throws IOException {
        new DockerService().getDockerClient().pingCmd().exec();
    }
}
