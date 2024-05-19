package com.mola.domain.tripBoard.repository;

import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.global.config.QueryDslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(QueryDslConfig.class)
@DataJpaTest
class TripPostRepositoryTest {

    @Autowired
    TripPostRepository tripPostRepository;
    @Test
    void findByIdWithOptimisticLock() {
        TripPost tripPost = new TripPost();
        TripPost save = tripPostRepository.save(tripPost);

        TripPost byIdWithOptimisticLock = tripPostRepository.findByIdWithOptimisticLock(save.getId());

        assertNotNull(byIdWithOptimisticLock);
    }
}