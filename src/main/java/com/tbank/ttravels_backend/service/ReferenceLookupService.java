package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.exception.CategoryNotFoundException;
import com.tbank.ttravels_backend.exception.MemberExpenseNotFoundException;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.exception.UserNotFoundInTravelException;
import com.tbank.ttravels_backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ReferenceLookupService {

    private final CategoryRepository categoryRepository;
    private final MemberExpenseRepository memberExpenseRepository;
    private final TravelRepository travelRepository;
    private final TravelMemberRepository travelMemberRepository;
    private final UserRepository userRepository;



    public ReferenceLookupService(CategoryRepository categoryRepository,
                                  MemberExpenseRepository memberExpenseRepository,
                                  TravelRepository travelRepository,
                                  TravelMemberRepository travelMemberRepository,
                                  UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.memberExpenseRepository = memberExpenseRepository;
        this.travelRepository = travelRepository;
        this.travelMemberRepository = travelMemberRepository;
        this.userRepository = userRepository;
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


    public Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(categoryId));
    }

    public void validateAllUsersInTravel(Long travelId, Set<Long> participantUserIds) {

        if (participantUserIds == null) return;

        participantUserIds.forEach(userId -> checkUserInTravel(userId, travelId));
    }

    public MemberExpense getMemberExpense(Long userId, Long expenseId){
        return memberExpenseRepository.findByParticipantIdAndExpenseId(userId, expenseId)
                .orElseThrow(() -> new MemberExpenseNotFoundException(userId));
    }

    public List<User> getUsers(Set<Long> userIds) {
        return this.userRepository.findAllById(userIds);
    }

    public List<Category> findAllCategoryInTravel(Long travelId){
        return this.categoryRepository.findAllByTravel_Id(travelId);
    }

    public void checkTravel(Long travelId) {
        travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException(travelId));
    }

    public List<User> findAllUsersInTravel(Long travelId) {
        return this.findTravel(travelId)
                .getTravelMembers()
                .stream()
                .map(TravelMember::getUser)
                .toList();
    }
}
