package com.mola.domain.tripBoard.tripPost.service;

import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.trip.repository.TripPlanRepository;
import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.like.repository.LikesRepository;
import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.domain.tripBoard.tripImage.repository.TripImageRepository;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TripPostService {

    private final TripPostRepository tripPostRepository;

    private final TripImageRepository tripImageRepository;

    private final TripPlanRepository tripPlanRepository;

    private final MemberRepository memberRepository;

    private final LikesRepository likesRepository;

    private final ModelMapper modelMapper;

    private final EntityManager em;

    private static final int MAX_RETRY = 3;

    private static final long RETRY_DELAY = 100;

    public Page<TripPostListResponseDto> getAllTripPosts(Pageable pageable) {
        return tripPostRepository.getAllTripPostResponseDto(null, TripPostStatus.PUBLIC, pageable);
    }

    public Page<TripPostListResponseDto> getAllMyPosts(Pageable pageable){
        Long memberId = getAuthenticatedMemberId();
        return tripPostRepository.getAllTripPostResponseDto(memberId, null, pageable);
    }

    public boolean isPublic(Long id) {
        return tripPostRepository.isPublic(id);
    }

    public boolean existsTripPost(Long id){
        return tripPostRepository.existsById(id);
    }

    public TripPostResponseDto getTripPostResponseDto(Long id){
        Long memberId = getAuthenticatedMemberId();
        return tripPostRepository.getTripPostResponseDtoById(id, memberId);
    }

    public TripPost findById(Long id){
        return tripPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTripPostIdentifier));
    }

    @Transactional
    public Map<String, Long> createDraftTripPost() {
        Long memberId = getAuthenticatedMemberId();
        Member member = em.getReference(Member.class, memberId);

        TripPost tripPost = TripPost.createDraft(member);
        Long tripPostId = tripPostRepository.save(tripPost).getId();

        return Map.of("memberId", memberId, "tempPostId", tripPostId);
    }

    @Transactional
    public Long save(TripPostDto tripPostDto) {
        if (!isOwner(tripPostDto.getId())) {
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        TripPost tripPost = extractAndSaveImageUrl(tripPostDto);
        return tripPost.getId();
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
        Long memberId = getAuthenticatedMemberId();
        validateTripPostAndMember(tripPostId, memberId, true);

        TripPost post = tripPostRepository.findByIdWithOptimisticLock(tripPostId);
        performLikesOperation(post, memberId, true);
    }

    @Transactional
    public void removeLikes(Long tripPostId) throws InterruptedException {
        Long memberId = getAuthenticatedMemberId();
        validateTripPostAndMember(tripPostId, memberId, false);

        TripPost post = tripPostRepository.findByIdWithOptimisticLock(tripPostId);
        performLikesOperation(post, memberId, false);
    }

    @Transactional
    public Page<CommentDto> getCommentsForTripPost(Long postId, Pageable pageable) {
        return tripPostRepository.getCommentsForTripPost(postId, pageable);
    }

    private Long getAuthenticatedMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        return Long.valueOf(authentication.getName());
    }

    private void validateTripPostAndMember(Long tripPostId, Long memberId, boolean isAdding) {
        if (!tripPostRepository.existsById(tripPostId)) {
            throw new CustomException(GlobalErrorCode.InvalidTripPostIdentifier);
        }
        if (isAdding && likesRepository.existsByMemberIdAndTripPostId(memberId, tripPostId)) {
            throw new CustomException(GlobalErrorCode.DuplicateLike);
        } else if (!isAdding && !likesRepository.existsByMemberIdAndTripPostId(memberId, tripPostId)) {
            throw new CustomException(GlobalErrorCode.BadRequest);
        }
    }

    private void performLikesOperation(TripPost post, Long memberId, boolean isAdding) throws InterruptedException {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidMemberIdentifierFormat));
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
                Thread.sleep(RETRY_DELAY);
                retryCount++;
            }
        }
        log.error("tripPostId: {}에 대한 최대 재시도 횟수 {}를 초과했습니다.", post.getId(), MAX_RETRY);
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

    public void validateTripPlan(Long tripPlanId){
        if(!tripPlanRepository.existsById(tripPlanId)){
            throw new CustomException(GlobalErrorCode.InvalidTripPlan);
        }
    }

    private MemberTripPostDto findValidMember(Long memberId) {
        return memberRepository.findMemberTripPostDtoById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.AccessDenied));
    }
    private TripPost extractAndSaveImageUrl(TripPostDto tripPostDto) {
        TripPost tripPost = findById(tripPostDto.getId());
        tripPost.toPublic();

        Document doc = Jsoup.parse(tripPostDto.getContent());
        Elements images = doc.select("img");

        if(!images.isEmpty()){
            String src = images.first().attr("src");
            tripPost.setRepresentationImageUrl(src);
            log.info("first image : {}", src);
        }

        Set<String> imageUrlsInContent = images.stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toSet());

        List<TripImage> tripImages = tripPost.getImageUrl();
        tripImages.forEach(tripImage -> {
            if (!imageUrlsInContent.contains(tripImage.getUrl())) {
                tripImage.setFlag(false);
                tripImageRepository.save(tripImage);
            }else {
                tripImage.setFlag(true);
            }
        });

        modelMapper.map(tripPostDto, tripPost);
        return tripPost;
    }
}
