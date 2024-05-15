package com.mola.domain.tripBoard.controller;

import com.mola.domain.tripBoard.entity.TripImage;
import com.mola.domain.tripBoard.service.TripImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/images")
public class ImageController {

    private final TripImageService tripImageService;

    @PostMapping
    public ResponseEntity<TripImage> uploadImage(@RequestParam("tripPlanId") Long id,
                                              @RequestParam MultipartFile file) {

        TripImage tripImage = tripImageService.save(id, file);

        return ResponseEntity.ok(tripImage);
    }
}
