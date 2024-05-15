package com.mola.domain.tripBoard.controller;

import com.mola.domain.tripBoard.dto.TripPostDto;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.service.TripPostService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public ResponseEntity<TripPost> saveTripPost(@Valid @RequestBody TripPostDto tripPostDto,
                                                 Errors errors){
        if(errors.hasErrors()){
            throw new CustomException(GlobalErrorCode.MissingRequireData);
        }

        TripPost save = tripPostService.save(tripPostDto);

        return ResponseEntity.ok(save);
    }



}
