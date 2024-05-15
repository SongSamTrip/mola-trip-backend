package com.mola.domain.tripBoard.repository;

import com.mola.domain.tripBoard.entity.TripPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPostRepository extends JpaRepository<TripPost, Long> {
}
