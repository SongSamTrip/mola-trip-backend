package com.mola.domain.tripBoard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    @ManyToOne
    private TripPost tripPost;

    public TripImage(String url, TripPost tripPost) {
        this.url = url;
        this.tripPost = tripPost;
    }

    public void setTripPostNull(){
        this.tripPost = null;
    }
}
