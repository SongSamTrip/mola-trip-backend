package com.mola.domain.tripBoard.tripImage.entity;

import com.mola.domain.tripBoard.tripImage.dto.TripImageDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class TripImage {

    @Id @GeneratedValue
    private Long id;
    private String url;
    @ManyToOne(fetch = FetchType.LAZY)
    private TripPost tripPost;

    private boolean flag;

    public TripImage(String url, TripPost tripPost) {
        this.url = url;
        this.tripPost = tripPost;
    }

    public TripImage(Long id, String url, TripPost tripPost) {
        this.id = id;
        this.url = url;
        this.tripPost = tripPost;
    }

    public TripImageDto toDto(){
        Long tripPostId = (this.getTripPost() != null) ? this.getTripPost().getId() : null;

        return TripImageDto.builder()
                .id(this.id)
                .url(this.url)
                .tripPostId(tripPostId)
                .build();
    }
    public void setTripPostNull(){
        this.tripPost = null;
        this.flag = false;
    }
}
