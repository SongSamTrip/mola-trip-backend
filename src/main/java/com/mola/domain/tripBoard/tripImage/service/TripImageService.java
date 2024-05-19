package com.mola.domain.tripBoard.tripImage.service;

import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripImage.repository.TripImageRepository;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

    @Transactional
    public TripImage save(Long id, MultipartFile file) {
        if(!tripPostService.existsTripPost(id)){
            throw new CustomException(GlobalErrorCode.InvalidTripPostIdentifier);
        }
        TripPost tripPost = entityManager.getReference(TripPost.class, id);
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
