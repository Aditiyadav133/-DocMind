package com.project.docmind.docmind.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.docmind.docmind.dto.RegisterUserRequest;
import com.project.docmind.docmind.dto.UserDto;
import com.project.docmind.docmind.entity.Role;
import com.project.docmind.docmind.entity.User;
import com.project.docmind.docmind.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserDto registerUser(@Valid RegisterUserRequest registerUserRequest) {
		if(registerUserRequest.username().isBlank()) throw new IllegalArgumentException("Username cannot be blank");
		else if(userRepository.existsByUsername(registerUserRequest.username())) throw new IllegalArgumentException("Username already exists");
		User user=new User();
		user.setEmail(registerUserRequest.email());
		user.setUsername(registerUserRequest.username());
		user.setPassword(
			    passwordEncoder.encode(registerUserRequest.password())
			);
		user.setRole(Role.USER);
		User savedUser=userRepository.save(user);
		return new UserDto(savedUser.getId(),savedUser.getUsername(),savedUser.getEmail(),savedUser.getRole());
	}

}
