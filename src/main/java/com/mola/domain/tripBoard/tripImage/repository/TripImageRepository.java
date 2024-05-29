package com.mola.domain.tripBoard.tripImage.repository;

import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripImageRepository extends JpaRepository<TripImage, Long>, TripImageRepositoryCustom {

    List<TripImage> findAllByFlag(boolean flag);
}
