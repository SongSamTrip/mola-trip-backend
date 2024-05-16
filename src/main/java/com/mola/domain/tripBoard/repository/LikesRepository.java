package com.mola.domain.tripBoard.repository;

import com.mola.domain.tripBoard.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    
}
