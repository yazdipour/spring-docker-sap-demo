package com.example.demo;

import com.github.dockerjava.api.command.BuildImageResultCallback;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/")
public class MainController {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(MainController.class);

    @Autowired
    DockerService dockerAPIService;

    /***
     * Controller method to handle the request to create a new image from a Dockerfile
     * @param dockerfile
     * @param tag
     * @return
     */
    @RequestMapping(path = "/create", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<String> create(@RequestParam("dockerfile") MultipartFile dockerfile, @RequestParam String tag) throws IOException {
        if (dockerfile == null) {
            throw new RuntimeException("You must select the a file for uploading");
        }
        String originalName = dockerfile.getOriginalFilename();
        long size = dockerfile.getSize();
        if (size == 0 || originalName == null) throw new RuntimeException("Invalid dockerfile");
        File tempFile = File.createTempFile(originalName, "");
        dockerfile.transferTo(tempFile);
        BuildImageResultCallback buildImageResultCallback = new BuildImageResultCallback();
        var imageId = dockerAPIService.getDockerClient().buildImageCmd()
                .withDockerfile(tempFile)
                .withTags(Collections.singleton(tag))
                .withPull(true)
                .exec(buildImageResultCallback).awaitImageId(2, TimeUnit.MINUTES);
        return new ResponseEntity<>(imageId, HttpStatus.OK);
    }

    /***
     * Controller method to ping the docker daemon
     */
    @RequestMapping(path = "/ping", method = RequestMethod.GET)
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
