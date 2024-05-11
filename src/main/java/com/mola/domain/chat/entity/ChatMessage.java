package com.mola.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Entity
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "trip_plan_id")
    private Long tripPlanId;
    @Column(name = "member_id")
    private Long memberId;
    @Column(name ="content")
    private String content;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
