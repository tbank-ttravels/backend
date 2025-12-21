package com.tbank.ttravels_backend.service;


import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getUsers(Set<Long> userIds) {
        return this.userRepository.findAllById(userIds);
    }
}