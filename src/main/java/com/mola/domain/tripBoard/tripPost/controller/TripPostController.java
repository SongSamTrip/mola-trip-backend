package com.mola.domain.tripBoard.tripPost.controller;

import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostUpdateDto;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tripPosts")
public class TripPostController {

    private final TripPostService tripPostService;


    @GetMapping
    public ResponseEntity<List<TripPostListResponseDto>> getTripPosts(Pageable pageable) {
        List<TripPostListResponseDto> allTripPosts =
                tripPostService.getAllTripPosts(pageable);

        return ResponseEntity.ok(allTripPosts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripPostResponseDto> getTripPost(@PathVariable Long id) {
        return ResponseEntity.ok(tripPostService.getTripPostResponseDto(id));
    }

    @PostMapping("/draft")
    public ResponseEntity<Map<String, Long>> createDraftTripPost(){
        return ResponseEntity.status(HttpStatus.CREATED).body(tripPostService.createDraftTripPost());
    }

    @PostMapping
    public ResponseEntity<TripPostResponseDto> saveTripPost(@Valid @RequestBody TripPostDto tripPostDto,
                                                 Errors errors){
        if(errors.hasErrors()){
            throw new CustomException(GlobalErrorCode.MissingRequireData);
        }

        TripPostResponseDto responseDto = tripPostService.save(tripPostDto);

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripPostResponseDto> updateTripPost(@Valid @RequestBody TripPostUpdateDto tripPostUpdateDto,
                                                              Errors errors) {
        if(errors.hasErrors()){
            throw new CustomException(GlobalErrorCode.MissingRequireData);
        }

        TripPostResponseDto responseDto = tripPostService.update(tripPostUpdateDto);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTripPost(@PathVariable Long id){
        tripPostService.deleteTripPost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<?> addLike(@PathVariable("id") Long tripPostId){
        try {
            tripPostService.addLikes(tripPostId);
        } catch (InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/likes")
    public ResponseEntity<?> removeLike(@RequestParam("id") Long tripPostId){
        try {
            tripPostService.removeLikes(tripPostId);
        } catch (InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }


}