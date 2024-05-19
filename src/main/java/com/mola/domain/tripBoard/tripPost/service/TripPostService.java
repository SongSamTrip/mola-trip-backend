package com.mola.domain.tripBoard.tripPost.service;

import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.like.repository.LikesRepository;
import com.mola.domain.tripBoard.tripImage.dto.TripImageDto;
import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.domain.tripBoard.tripImage.repository.TripImageRepository;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostUpdateDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.persistence.EntityManager;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TripPostService {

    private final TripPostRepository tripPostRepository;

    private final TripImageRepository tripImageRepository;

    private final MemberRepository memberRepository;

    private final LikesRepository likesRepository;

    private final ModelMapper modelMapper;

    private final EntityManager em;

    private static final int MAX_RETRY = 3;

    private static final long RETRY_DELAY = 100;

    public List<TripPostListResponseDto> getAllTripPosts(Pageable pageable) {
        Page<TripPost> all = tripPostRepository.findAll(pageable);
        return all.stream().map(TripPost::toTripPostListResponseDto).collect(Collectors.toList());
    }

    public boolean isPublic(Long id) {
        return tripPostRepository.isPublic(id);
    }

    public boolean existsTripPost(Long id){
        return tripPostRepository.existsById(id);
    }

    public TripPostResponseDto getTripPostResponseDto(Long id){
        return tripPostRepository.getTripPostResponseDtoById(id);
    }

    public TripPost findById(Long id){
        return tripPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTripPostIdentifier));
    }

    @Transactional
    public Map<String, Long> createDraftTripPost() {
        Long memberId = getMemberId();
        Member member = em.getReference(Member.class, memberId);

        TripPost tripPost = TripPost.createDraft(member);
        Long tripPostId = tripPostRepository.save(tripPost).getId();

        return Map.of("memberId", memberId, "tempPostId", tripPostId);
    }

    @Transactional
    public TripPostResponseDto save(TripPostDto tripPostDto){
        tripPostDto.getTripImageDtos().forEach(t -> {
            tripImageRepository.toPublicImages(t.getId());
        });

        TripPost byId = findById(tripPostDto.getId());

        modelMapper.map(tripPostDto, byId);
        TripPostResponseDto dto = modelMapper.map(byId, TripPostResponseDto.class);
        dto.setMemberId(byId.getMember().getId());
        dto.setNickname(byId.getMember().getNickname());

        return dto;
    }

    @Transactional
    public TripPostResponseDto update(TripPostUpdateDto tripPostUpdateDto) {
        if (!isOwner(tripPostUpdateDto.getId())) {
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        TripPost tripPost = findById(tripPostUpdateDto.getId());
        Set<Long> collect = tripPostUpdateDto.getTripImageList().stream().map(TripImageDto::getId).collect(Collectors.toSet());

        List<TripImage> tripImages = filterAndAssignTripImages(tripPost, collect);
        tripPost.setImageUrl(tripImages);
        modelMapper.map(tripPostUpdateDto, tripPost);
        return tripPostRepository.getTripPostResponseDtoById(tripPostRepository.save(tripPost).getId());
    }

    private List<TripImage> filterAndAssignTripImages(TripPost tripPost, Set<Long> collect) {
        return tripPost.getImageUrl().stream().filter(tripImage -> {
            if (!collect.contains(tripImage.getId())) {
                tripImage.setTripPostNull();
                return false;
            }
            return true;
        }).collect(Collectors.toList());
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

        Long memberId = getMemberId();

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

        Long memberId = getMemberId();

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

    private MemberTripPostDto findValidMember(Long memberId) {
        return memberRepository.findMemberTripPostDtoById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.AccessDenied));
    }

    public Long getMemberId() {
        Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        return memberId;
    }
}
