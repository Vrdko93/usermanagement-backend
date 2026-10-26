package com.syn.usermanagement.service;

import com.syn.usermanagement.dto.UserResponse;
import com.syn.usermanagement.entity.User;
import com.syn.usermanagement.exception.EmailAlreadyExistsException;
import com.syn.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @CacheEvict(value = "users", allEntries = true)
    public UserResponse registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already exists: " + user.getEmail()
            );
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Cacheable(value = "users")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }
}
