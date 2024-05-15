package com.mola.domain.tripBoard.controller;

import com.mola.domain.tripBoard.service.TripPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tripPosts")
public class TripPostController {

    private final TripPostService tripPostService;

    @PostMapping("/draft")
    public ResponseEntity<Long> createDraftTripPost(){
        return ResponseEntity.status(HttpStatus.CREATED).body(tripPostService.createDraftTripPost());
    }



}
