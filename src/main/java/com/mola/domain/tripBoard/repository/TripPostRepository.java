package com.mola.domain.tripBoard.repository;

import com.mola.domain.tripBoard.entity.TripPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPostRepository extends JpaRepository<TripPost, Long> {

    TripPost save(TripPost tripPost);

    Page<TripPost> findAll(Pageable pageable);
}
