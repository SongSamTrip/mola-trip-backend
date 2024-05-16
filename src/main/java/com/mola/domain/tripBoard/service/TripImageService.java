package com.mola.domain.tripBoard.service;

import com.mola.domain.tripBoard.entity.TripImage;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.repository.TripImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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



    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOrphanImages() {
        List<TripImage> allByTripPostId = tripImageRepository.findAllByTripPostId(null);

        allByTripPostId.forEach(tripImage -> {
            imageService.delete(tripImage.getUrl());
        });
    }


}
