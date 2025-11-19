package com.tbank.ttravels_backend.factory;

import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.TravelStatus;

import java.util.HashSet;

public final class TravelFactory {

    private TravelFactory() {
        throw new UnsupportedOperationException();
    }

    public static Travel createTravel(CreateTravelRequest request, User owner) {
        return Travel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(TravelStatus.ACTIVE)
                .owner(owner)
                .travelMembers(new HashSet<>())
                .build();
    }
}
