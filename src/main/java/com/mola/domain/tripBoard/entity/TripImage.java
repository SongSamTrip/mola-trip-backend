package com.mola.domain.tripBoard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class TripImage {

    @Id @GeneratedValue
    private Long id;
    private String url;
    @ManyToOne(fetch = FetchType.LAZY)
    private TripPost tripPost;

    public TripImage(String url, TripPost tripPost) {
        this.url = url;
        this.tripPost = tripPost;
    }

    public void setTripPostNull(){
        this.tripPost = null;
    }
}
