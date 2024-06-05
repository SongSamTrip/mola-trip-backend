package com.mola.domain.tripBoard.like.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.like.repository.LikesRepository;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import com.mola.global.util.SecurityUtil;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikesService {

    private final LikesRepository likesRepository;

    private final MemberRepository memberRepository;

    private final TripPostRepository tripPostRepository;

    private final SecurityUtil securityUtil;

    private static final int MAX_RETRY = 3;

    private static final long RETRY_DELAY = 100;


    @Transactional
    public void addLikes(Long tripPostId)  {
        Long memberId = getAuthenticatedMemberId();
        validateTripPostAndMember(tripPostId, memberId, true);

        TripPost post = tripPostRepository.findByIdWithOptimisticLock(tripPostId);
        performLikesOperation(post, memberId, true);
    }

    @Transactional
    public void removeLikes(Long tripPostId)  {
        Long memberId = getAuthenticatedMemberId();
        validateTripPostAndMember(tripPostId, memberId, false);

        TripPost post = tripPostRepository.findByIdWithOptimisticLock(tripPostId);
        performLikesOperation(post, memberId, false);
    }

    private Long getAuthenticatedMemberId() {
        return securityUtil.getAuthenticatedMemberId();
    }

    private void validateTripPostAndMember(Long tripPostId, Long memberId, boolean isAdding) {
        if (!tripPostRepository.existsById(tripPostId)) {
            throw new CustomException(GlobalErrorCode.InvalidTripPostIdentifier);
        }
        if (isAdding && likesRepository.existsByMemberIdAndTripPostIdImpl(memberId, tripPostId)) {
            throw new CustomException(GlobalErrorCode.DuplicateLike);
        } else if (!isAdding && !likesRepository.existsByMemberIdAndTripPostIdImpl(memberId, tripPostId)) {
            throw new CustomException(GlobalErrorCode.BadRequest);
        }
    }

    private void performLikesOperation(TripPost post, Long memberId, boolean isAdding) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidMemberIdentifierFormat));
        int retryCount = 0;

        while (retryCount < MAX_RETRY) {
            try {
                if (isAdding) {
                    Likes likes = new Likes();
                    likes.setMember(member);
                    likes.setTripPost(post);
                    post.addLikes(likes);
                    member.addLikes(likes);
                    likesRepository.save(likes);
                } else {
                    Likes likes = likesRepository.findByMemberIdAndTripPostId(memberId, post.getId());
                    post.deleteLikes(likes);
                    member.deleteLikes(likes);
                    likesRepository.delete(likes);
                }
                tripPostRepository.save(post);
                return;
            } catch (OptimisticLockException e) {
                log.info("tripPostId: {} 충돌 발생, 재시도 중...", post.getId());
                post = tripPostRepository.findByIdWithOptimisticLock(post.getId());
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ex) {
                    throw new CustomException(GlobalErrorCode.ExcessiveRetries);
                }
                retryCount++;
            }
        }
        log.error("tripPostId: {}에 대한 최대 재시도 횟수 {}를 초과했습니다.", post.getId(), MAX_RETRY);
        throw new CustomException(GlobalErrorCode.ExcessiveRetries);
    }
}
