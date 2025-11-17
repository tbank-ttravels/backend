package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.repository.CategoryRepository;
import com.tbank.ttravels_backend.repository.TravelMemberRepository;
import com.tbank.ttravels_backend.repository.TravelRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ReferenceLookupService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TravelRepository travelRepository;
    private final TravelMemberRepository travelMemberRepository;



    public ReferenceLookupService(CategoryRepository categoryRepository, UserRepository userRepository,
                                  TravelRepository travelRepository, TravelMemberRepository travelMemberRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.travelRepository = travelRepository;
        this.travelMemberRepository = travelMemberRepository;
    }

    // 💡 TODO Можно оптимизировать, чтобы не делать три запроса, а сразу искать в TravelMemberRepository с join fetch.
    public User findUserInTravel(Long userId, Long travelId) {

        // TODO нужны ли эти запросы?
//        checkUserExists(userId);
//        checkTravelExists(travelId);

        return travelMemberRepository.findByUserIdAndTravelId(userId, travelId)
                .orElseThrow(() ->
                        new UserNotFoundInTravelException(userId, travelId))
                .getUser();
    }

    public void checkUserInTravel(Long userId, Long travelId){

        // TODO надо ли
//        checkUserExists(userId);
//        checkTravelExists(travelId);

        if(!travelMemberRepository.existsByUserIdAndTravelId(userId, travelId))
            throw new UserNotFoundInTravelException(userId, travelId);
    }

    public Travel findTravel(Long travelId) {
        return travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException(travelId));
    }

    private void checkTravelExists(Long travelId) {
        if(!travelRepository.existsById(travelId))
            throw new TravelNotFoundException(travelId);
    }

    public void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(userId);
    }


    public Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(categoryId));
    }

    public void validateAllUsersInTravel(Long travelId, Set<Long> participantUserIds) {

        if (participantUserIds == null) return;

        participantUserIds.forEach(userId -> checkUserInTravel(userId, travelId));
    }
}
