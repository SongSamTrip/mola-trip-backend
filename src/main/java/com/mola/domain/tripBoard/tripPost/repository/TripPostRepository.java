package com.mola.domain.tripBoard.tripPost.repository;

import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripPostRepository extends JpaRepository<TripPost, Long>, TripPostRepositoryCustom {

    TripPost save(TripPost tripPost);

    Page<TripPost> findAll(Pageable pageable);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT tp FROM TripPost tp WHERE tp.id = :id")
    TripPost findByIdWithOptimisticLock(@Param("id") Long id);
}
