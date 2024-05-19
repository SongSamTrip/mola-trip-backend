package com.mola.domain.tripBoard.tripImage.repository;

import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TripImageRepository extends JpaRepository<TripImage, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE TripImage ti SET ti.flag = false WHERE ti.tripPost.id = :tripPostId")
    void detachOldImages(Long tripPostId);

    @Query("SELECT ti FROM TripImage ti WHERE ti.tripPost.id = :tripPostId")
    List<TripImage> findAllByTripPostId(Long tripPostId);

    @Modifying
    @Transactional
    @Query("UPDATE TripImage ti SET ti.flag = true WHERE ti.id = :tripImageId")
    void toPublicImages(@Param("tripImageId") Long tripImageId);
}
