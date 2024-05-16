package com.mola.domain.tripBoard.service;

import com.mola.domain.tripBoard.dto.*;
import com.mola.domain.tripBoard.entity.TripImage;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.repository.TripImageRepository;
import com.mola.domain.tripBoard.repository.TripPostRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TripPostService {

    private final TripPostRepository tripPostRepository;
    private final TripImageRepository tripImageRepository;
    private final ModelMapper modelMapper;

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

    public boolean isOwner(Long id){
        TripPost byId = findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!authentication.isAuthenticated()){
            return false;
        }

        return authentication.getName().equals(String.valueOf(byId.getMember().getId()));
    }
}
