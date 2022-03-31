package com.example.demo;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PushResponseItem;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(path = "/image")
public class ImageController {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ImageController.class);

    @Autowired
    DockerService dockerAPIService;

    /***
     * Controller to create a new image from a Dockerfile
     * @param dockerfile
     * @param tag
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<String> create(@RequestParam("dockerfile") MultipartFile dockerfile, @RequestParam String tag) throws IOException {
        if (dockerfile == null) {
            throw new RuntimeException("You must select the a file for uploading");
        }
        String originalName = dockerfile.getOriginalFilename();
        long size = dockerfile.getSize();
        if (size == 0 || originalName == null) throw new RuntimeException("Invalid dockerfile");
        File tempFile = File.createTempFile(originalName, "");
        dockerfile.transferTo(tempFile);
        BuildImageResultCallback resultCallback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                super.onNext(item);
                logger.info(item.getStream());
            }
        };
        var imageId = dockerAPIService.getDockerClient()
                .buildImageCmd()
                .withDockerfile(tempFile)
                .withTags(Collections.singleton(tag))
                .withPull(true)
                .exec(resultCallback).awaitImageId();
        return new ResponseEntity<>(imageId, HttpStatus.OK);
    }


    /***
     * Controller method to remove an image
     * @param imageId
     * @return String
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> remove(@RequestParam String imageId) {
        try {
            dockerAPIService.getDockerClient().removeImageCmd(imageId).exec();
            return new ResponseEntity<>("removed", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while removing the image", e);
            return new ResponseEntity<>("not removed", HttpStatus.BAD_REQUEST);
        }
    }

    /***
     * Controller to get list of images
     * @return List<Image>
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Image>> getImages() {
        try {
            return new ResponseEntity<>(dockerAPIService.getDockerClient().listImagesCmd().exec(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while getting the list of images", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /***
     * Push an image to a registry
     * @param imageId
     * @param tag
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<String> push(@RequestParam String imageId, @RequestParam String tag) {
        try {
            ResultCallback<PushResponseItem> callBack = new ResultCallback<>() {
                @Override
                public void close() {
                    logger.info("Closing");
                }

                @Override
                public void onStart(Closeable closeable) {
                    logger.info("Starting");
                }

                @Override
                public void onNext(PushResponseItem pushResponseItem) {
                    logger.info(pushResponseItem.getStatus());
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.error("Error while pushing the image", throwable);
                }

                @Override
                public void onComplete() {
                    logger.info("Completed");
                }
            };
            dockerAPIService
                    .getDockerClient()
                    .pushImageCmd(imageId)
                    .withTag(tag)
                    .exec(callBack);
            return new ResponseEntity<>("Trying to push", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while pushing the image", e);
            return new ResponseEntity<>("not pushed", HttpStatus.BAD_REQUEST);
        }
    }
}

