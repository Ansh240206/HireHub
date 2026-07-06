package com.hirehub.service;

import com.hirehub.dto.user.UserResponse;
import com.hirehub.entity.User;
import com.hirehub.enums.RoleName;
import com.hirehub.exception.ResourceNotFoundException;
import com.hirehub.mapper.UserMapper;
import com.hirehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(RoleName role, Pageable pageable) {
        Page<User> users = role == null ? userRepository.findAll(pageable) : userRepository.findByRole(role, pageable);
        return users.map(UserMapper::toResponse);
    }

    @Transactional
    public UserResponse setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(enabled);
        return UserMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }
}
