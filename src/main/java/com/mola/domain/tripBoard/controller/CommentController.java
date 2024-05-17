package com.mola.domain.tripBoard.controller;


import com.mola.domain.tripBoard.dto.CommentDto;
import com.mola.domain.tripBoard.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/tripPosts/{tripPostId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable("tripPostId") Long tripPostId,
                                                        Pageable pageable) {
        List<CommentDto> allComments = commentService.getAllComments(tripPostId, pageable);

        return ResponseEntity.ok(allComments);
    }

    @PostMapping("/tripPosts/{tripPostId}/comments")
    public ResponseEntity<CommentDto> saveComment(@PathVariable("tripPostId") Long tripPostId,
                                                  @RequestBody CommentDto commentDto){

        CommentDto save = commentService.save(tripPostId, commentDto);

        return ResponseEntity.ok(save);
    }

    @PutMapping("/tripPosts/{tripPostId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable("tripPostId") Long tripPostId,
                                                    @PathVariable("commentId") Long commentId,
                                                    @RequestBody CommentDto commentDto){
        CommentDto update = commentService.update(tripPostId, commentId, commentDto);

        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/tripPosts/{tripPostId}/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable("tripPostId") Long tripPostId,
                                                @PathVariable("commentId") Long commentId) {
        commentService.delete(tripPostId, commentId);

        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}
