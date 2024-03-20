package com.nikonenko.e2etests.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PassengerResponse {
    private UUID id;
    private String username;
    private String phone;
    private Set<RatingPassengerResponse> ratingSet;
}