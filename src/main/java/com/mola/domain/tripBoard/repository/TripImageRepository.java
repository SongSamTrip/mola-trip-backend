package com.mola.domain.tripBoard.repository;

import com.mola.domain.tripBoard.entity.TripImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TripImageRepository extends JpaRepository<TripImage, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE TripImage ti SET ti.tripPost = null WHERE ti.tripPost.id = :tripPostId")
    void detachOldImages(Long tripPostId);
}
