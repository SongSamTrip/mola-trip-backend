package com.mola.domain.tripBoard.repository;

import com.mola.domain.tripBoard.entity.TripPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TripPostRepository extends JpaRepository<TripPost, Long> {

    TripPost save(TripPost tripPost);

    Page<TripPost> findAll(Pageable pageable);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT tp FROM TripPost tp WHERE tp.id = :id")
    TripPost findByIdWithOptimisticLock(Long id);
}
