package com.mola.domain.tripBoard.tripPost.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.comment.dto.CommentDto;
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
import com.mola.global.util.SecurityUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final SecurityUtil securityUtil;

    private final ModelMapper modelMapper;

    private final EntityManager em;


    /**
     * 모든 공개 게시글을 페이징 처리하여 반환합니다.
     * @param pageable
     * @return 게시글 리스트 DTO 를 페이징 처리하여 반환합니다.
     */
    public Page<TripPostListResponseDto> getAllPublicTripPosts(Pageable pageable) {
        return tripPostRepository.getAllTripPostResponseDto(null, TripPostStatus.PUBLIC, pageable);
    }

    /**
     * 현재 인증된 사용자가 작성한 모든 게시글을 페이징 처리하여 반환합니다.
     * @param pageable
     * @return 게시글 리스트 DTO 를 페이징 처리하여 반환합니다.
     */
    public Page<TripPostListResponseDto> getAllMyPosts(Pageable pageable){
        Long memberId = getAuthenticatedMemberId();
        return tripPostRepository.getAllTripPostResponseDto(memberId, null, pageable);
    }

    /**
     * 관리자 권한으로 모든 게시글을 페이징 처리하여 반환합니다.
     * @param pageable
     * @return 게시글 리스트 DTO 를 페이징 처리하여 반환합니다.
     */
    public Page<TripPostListResponseDto> adminGetAllPosts(Pageable pageable){
        return tripPostRepository.getAllTripPostResponseDto(null, null, pageable);
    }

    public boolean isTripPostStatusPublic(Long tripPostId) {
        return tripPostRepository.isTripPostStatusPublic(tripPostId);
    }

    public boolean existsTripPost(Long tripPostId){
        return tripPostRepository.existsById(tripPostId);
    }

    /**
     * tripPostId 에 해당하는 엔티티를 가져와 게시글 상세페이지에 필요한 dto 클래스로 변환하여 반환
     * @param tripPostId TripPost의 식별자
     * @return TripPostResponseDto 게시글 상세 정보를 포함하고 있는 DTO 클래스
     * @throws CustomException 만약 접근 권한이 없다면 AccessDenied 에러를 발생
     */
    public TripPostResponseDto getTripPostResponseDto(Long tripPostId){
        Long memberId = getAuthenticatedMemberId();

        validateAccessToTripPost(tripPostId, memberId);

        return tripPostRepository.getTripPostResponseDtoById(tripPostId, memberId);
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
        if (!isCurrentUserOwnerOfTripPost(tripPostDto.getId())) {
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        TripPost tripPost = extractAndSaveImageUrl(tripPostDto);
        return tripPost.getId();
    }


    @Transactional
    public void deleteTripPost(Long id){
        if(!isCurrentUserOwnerOfTripPost(id) && !isMemberAdmin(getAuthenticatedMemberId())){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }

        deleteById(id);
    }

    @Transactional
    public void deleteAdminTripPost(Long id){
        deleteById(id);
    }

    public Page<CommentDto> getCommentsForTripPost(Long postId, Pageable pageable) {
        return tripPostRepository.getCommentsForTripPost(postId, pageable);
    }

    private Long getAuthenticatedMemberId() {
        return securityUtil.getAuthenticatedMemberId();
    }


    private boolean isCurrentUserOwnerOfTripPost(Long tripPostId){
        TripPost byId = findById(tripPostId);

        return securityUtil.getAuthenticatedMemberId() == byId.getMember().getId();
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

    private void deleteById(Long id) {
        TripPost byId = findById(id);
        byId.getImageUrl().forEach(TripImage::setTripPostNull);

        tripPostRepository.delete(byId);
    }

    private void validateAccessToTripPost(Long tripPostId, Long memberId) {
        if(!tripPostRepository.isTripPostStatusPublic(tripPostId)){
            if(!isCurrentUserOwnerOfTripPost(memberId) && !isMemberAdmin(memberId)){
                throw new CustomException(GlobalErrorCode.AccessDenied);
            }
        }
    }

    private boolean isMemberAdmin(Long memberId) {
        return securityUtil.isAdmin(memberId);
    }
}
