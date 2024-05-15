package com.mola.domain.tripBoard.service;

import com.mola.domain.tripBoard.dto.TripPostDto;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.repository.TripPostRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TripPostService {

    private final TripPostRepository tripPostRepository;
    private final ModelMapper modelMapper;

    public boolean existsTripPost(Long id){
        return tripPostRepository.existsById(id);
    }

    public TripPost findById(Long id){
        return tripPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTripPostIdentifier));
    }

    public Long createDraftTripPost(){
        return tripPostRepository.save(TripPost.createDraft()).getId();
    }

    public TripPost save(TripPostDto tripPostDto){
        TripPost tripPost = modelMapper.map(tripPostDto, TripPost.class);
        tripPost.toPublic();

        return tripPostRepository.save(tripPost);
    }
}
