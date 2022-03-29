package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    private final Logger logger = LoggerFactory.getLogger(StatusController.class);
    private final DockerService dockerAPIService;

    @Autowired
    public StatusController(DockerService dockerAPIService) {
        this.dockerAPIService = dockerAPIService;
    }

    /***
     * Controller method to ping the docker daemon
     */
    @RequestMapping(path = "/ping")
    public ResponseEntity<String> ping() {
        try {
            dockerAPIService.getDockerClient().pingCmd().exec();
            return new ResponseEntity<>("connected to docker daemon", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while pinging the docker daemon", e);
            return new ResponseEntity<>("not connected", HttpStatus.REQUEST_TIMEOUT);
        }
    }
}
