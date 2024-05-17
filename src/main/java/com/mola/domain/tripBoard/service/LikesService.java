package com.mola.domain.tripBoard.service;

import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.tripBoard.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LikesService {

    private final LikesRepository likesRepository;


}
