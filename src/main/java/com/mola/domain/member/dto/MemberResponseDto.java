package com.mola.domain.member.dto;

import com.mola.domain.member.entity.LoginProvider;
import com.mola.domain.member.entity.MemberRole;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class MemberResponseDto {

    private Long id;

    private String nickname;

    private String personalId;

    private String profileImageUrl;

    private LoginProvider loginProvider;

    private MemberRole memberRole;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;
}
