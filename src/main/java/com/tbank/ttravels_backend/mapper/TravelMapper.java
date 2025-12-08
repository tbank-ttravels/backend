package com.tbank.ttravels_backend.mapper;

import com.tbank.ttravels_backend.dto.travel.MyTravelItem;
import com.tbank.ttravels_backend.dto.travel.MyTravelsResponse;
import com.tbank.ttravels_backend.dto.travel.TravelResponse;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TravelMapper {

    public TravelResponse toTravelResponse(Travel travel) {
        return new TravelResponse(
                travel.getId(),
                travel.getName(),
                travel.getDescription(),
                travel.getStartDate(),
                travel.getEndDate(),
                travel.getStatus()
        );
    }

    public MyTravelItem toMyTravelItem(TravelMember member) {
        Travel travel = member.getTravel();
        return new MyTravelItem(
                travel.getId(),
                travel.getName(),
                travel.getDescription(),
                travel.getStartDate(),
                travel.getEndDate(),
                travel.getStatus(),
                member.getRole()
        );
    }

    public MyTravelsResponse toMyTravelsResponse(List<TravelMember> memberships) {
        List<MyTravelItem> items = memberships.stream()
                .map(this::toMyTravelItem)
                .collect(Collectors.toList());
        return new MyTravelsResponse(items);
    }
}
