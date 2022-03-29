package com.example.demo;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/container")
public class ContainerController {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ContainerController.class);

    private final DockerService dockerAPIService;

    @Autowired
    public ContainerController(DockerService dockerAPIService) {
        this.dockerAPIService = dockerAPIService;
    }

    /***
     * Create a new container
     * @return
     */
    @RequestMapping(path = "/create")
    public String createContainer() {
        logger.info("createContainer()");
        return dockerAPIService.getDockerClient().createContainerCmd("busybox").withCmd("sleep", "9999").exec().toString();
    }
}
