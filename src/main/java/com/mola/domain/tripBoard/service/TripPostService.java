package com.mola.domain.tripBoard.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.dto.*;
import com.mola.domain.tripBoard.entity.Likes;
import com.mola.domain.tripBoard.entity.TripImage;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.repository.LikesRepository;
import com.mola.domain.tripBoard.repository.TripPostRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TripPostService {

    private final TripPostRepository tripPostRepository;

    private final MemberRepository memberRepository;

    private final LikesRepository likesRepository;

    private final ModelMapper modelMapper;

    private static final int MAX_RETRY = 3;

    private static final long RETRY_DELAY = 100;

    public List<TripPostListResponseDto> getAllTripPosts(Pageable pageable) {
        Page<TripPost> all = tripPostRepository.findAll(pageable);

        List<TripPostListResponseDto> list = new ArrayList<>();
        all.forEach(tripPost -> {
            list.add(TripPost.toTripPostListResponseDto(tripPost));
        });

        return list;
    }

    public boolean existsTripPost(Long id){
        return tripPostRepository.existsById(id);
    }

    public TripPost findById(Long id){
        return tripPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTripPostIdentifier));
    }

    @Transactional
    public Long createDraftTripPost(){
        return tripPostRepository.save(TripPost.createDraft()).getId();
    }

    @Transactional
    public TripPost save(TripPostDto tripPostDto){
        TripPost tripPost = modelMapper.map(tripPostDto, TripPost.class);
        tripPost.toPublic();

        return tripPostRepository.save(tripPost);
    }

    @Transactional
    public TripPostResponseDto update(TripPostUpdateDto tripPostUpdateDto){
        if(!isOwner(tripPostUpdateDto.getId())){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        TripPost tripPost = findById(tripPostUpdateDto.getId());

        Set<Long> collect = tripPostUpdateDto.getTripImageList().stream()
                .map(TripImageDto::getId)
                .collect(Collectors.toSet());

        List<TripImage> tripImages = new ArrayList<>();

        tripPost.getImageUrl().forEach(tripImage -> {
            if(!collect.contains(tripImage.getId())){
                tripImage.setTripPostNull();
            } else {
                tripImages.add(tripImage);
            }
        });

        tripPost.setImageUrl(tripImages);

        modelMapper.map(tripPostUpdateDto, tripPost);
        TripPost save = tripPostRepository.save(tripPost);

        return TripPost.toTripPostResponseDto(save);
    }

    @Transactional
    public void deleteTripPost(Long id){
        if(!isOwner(id)){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }

        TripPost byId = findById(id);
        byId.getImageUrl().forEach(TripImage::setTripPostNull);
        tripPostRepository.delete(byId);
    }


    @Transactional
    public void addLikes(Long tripPostId) throws InterruptedException {
        int retryCount = 0;

        Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        if(!tripPostRepository.existsById(tripPostId)){
            throw new CustomException(GlobalErrorCode.InvalidTripPostIdentifier);
        }

        if(likesRepository.existsByMemberIdAndTripPostId(memberId, tripPostId)){
            throw new CustomException(GlobalErrorCode.DuplicateLike);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidMemberIdentifierFormat));


        while(retryCount < MAX_RETRY) {
            try {
                TripPost post = tripPostRepository.findByIdWithOptimisticLock(tripPostId);
                Likes likes = new Likes();
                likes.setMember(member);
                likes.setTripPost(post);

                post.addLikes(likes);
                member.addLikes(likes);
                likesRepository.save(likes);
                tripPostRepository.save(post);

                return;
            } catch (OptimisticLockException e) {
                log.info("tripPostId: {} 충돌 발생", tripPostId);
                Thread.sleep(RETRY_DELAY);
                retryCount++;
            }
        }

        log.error("tripPostId: {}에 대한 최대 재시도 횟수 {}를 초과했습니다.", tripPostId, MAX_RETRY);
        throw new CustomException(GlobalErrorCode.ExcessiveRetries);
    }


    @Transactional
    public void removeLikes(Long tripPostId) throws InterruptedException {
        int retryCount = 0;

        Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        if(!tripPostRepository.existsById(tripPostId)){
            throw new CustomException(GlobalErrorCode.InvalidTripPostIdentifier);
        }

        if(!likesRepository.existsByMemberIdAndTripPostId(memberId, tripPostId)){
            throw new CustomException(GlobalErrorCode.BadRequest);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidMemberIdentifierFormat));


        while(retryCount < MAX_RETRY) {
            try {
                TripPost post = tripPostRepository.findByIdWithOptimisticLock(tripPostId);
                Likes likes = likesRepository.findByMemberIdAndTripPostId(memberId, tripPostId);

                post.deleteLikes(likes);
                member.deleteLikes(likes);
                likesRepository.delete(likes);
                tripPostRepository.save(post);

                return;
            } catch (OptimisticLockException e) {
                log.info("tripPostId: {} 충돌 발생", tripPostId);
                Thread.sleep(RETRY_DELAY);
                retryCount++;
            }
        }

        log.error("tripPostId: {}에 대한 최대 재시도 횟수 {}를 초과했습니다.", tripPostId, MAX_RETRY);
        throw new CustomException(GlobalErrorCode.ExcessiveRetries);
    }

    public boolean isOwner(Long id){
        TripPost byId = findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!authentication.isAuthenticated()){
            return false;
        }

        return authentication.getName().equals(String.valueOf(byId.getMember().getId()));
    }
}
