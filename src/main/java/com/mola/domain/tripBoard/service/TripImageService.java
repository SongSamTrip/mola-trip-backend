package com.mola.domain.tripBoard.service;

import com.mola.domain.tripBoard.entity.TripImage;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.repository.TripImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class TripImageService {

    private final TripImageRepository tripImageRepository;
    private final TripPostService tripPostService;
    private final ImageService imageService;

    public TripImage save(Long id, MultipartFile file) {
        TripPost tripPost = tripPostService.findById(id);
        String imageUrl = imageService.upload(file);

        return tripImageRepository.save(new TripImage(imageUrl, tripPost));
    }


}
