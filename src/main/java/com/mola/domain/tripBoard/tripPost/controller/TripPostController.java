package com.mola.domain.tripBoard.tripPost.controller;

import com.mola.domain.tripBoard.like.service.LikesService;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(value = "/tripPosts")
public class TripPostController {

    private final TripPostService tripPostService;

    private final LikesService likesService;


    @GetMapping
    public ResponseEntity<Page<TripPostListResponseDto>> getTripPosts(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<TripPostListResponseDto> allTripPosts = tripPostService.getAllPublicTripPosts(pageable);
        return ResponseEntity.ok(allTripPosts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripPostResponseDto> getTripPost(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tripPostService.getTripPostResponseDto(id));
    }

    @GetMapping("/myPosts")
    public ResponseEntity<Page<TripPostListResponseDto>> getMyTripPosts(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<TripPostListResponseDto> allTripPosts = tripPostService.getAllMyPosts(pageable);
        return ResponseEntity.ok(allTripPosts);
    }

    @PostMapping("/draft")
    public ResponseEntity<Map<String, Long>> createDraftTripPost(){
        log.info("call createDraftTripPost");
        return ResponseEntity.status(HttpStatus.CREATED).body(tripPostService.createDraftTripPost());
    }

    @PostMapping
    public ResponseEntity<Long> saveTripPost(@Valid @RequestBody TripPostDto tripPostDto,
                                             Errors errors){
        if(errors.hasErrors()){
            throw new CustomException(GlobalErrorCode.MissingRequireData);
        }

        log.info("{}", tripPostDto);

        return ResponseEntity.ok(tripPostService.save(tripPostDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTripPost(@PathVariable("id") Long id){
        tripPostService.deleteTripPost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<?> addLike(@PathVariable("id") Long tripPostId){
        likesService.addLikes(tripPostId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/likes")
    public ResponseEntity<?> removeLike(@PathVariable("id") Long tripPostId){
        likesService.removeLikes(tripPostId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<Page<TripPostListResponseDto>> getAdminTripPosts(Pageable pageable){
        return ResponseEntity.ok(tripPostService.adminGetAllPosts(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}/admin")
    public ResponseEntity<?> deleteAdminTripPosts(@PathVariable("id") Long id){
        tripPostService.deleteAdminTripPost(id);
        return ResponseEntity.ok().build();
    }

}
