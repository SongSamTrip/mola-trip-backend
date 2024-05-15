package com.mola.domain.tripBoard.service;

import com.mola.domain.tripBoard.dto.TripImageDto;
import com.mola.domain.tripBoard.dto.TripPostDto;
import com.mola.domain.tripBoard.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.dto.TripPostUpdateDto;
import com.mola.domain.tripBoard.entity.TripImage;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.repository.TripImageRepository;
import com.mola.domain.tripBoard.repository.TripPostRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        TripPost tripPost = findById(tripPostUpdateDto.getId());
        List<TripImage> allByTripPostId = tripImageRepository.findAllByTripPostId(tripPostUpdateDto.getId());

        Set<Long> collect = tripPostUpdateDto.getTripImageList().stream()
                .map(TripImageDto::getId)
                .collect(Collectors.toSet());

        allByTripPostId.forEach(tripImage -> {
            if(!collect.contains(tripImage.getId())){
                tripImage.setTripPostNull();
            }
        });

        modelMapper.map(tripPostUpdateDto, tripPost);
        TripPost save = tripPostRepository.save(tripPost);
        return TripPost.fromEntity(save);
    }
}
