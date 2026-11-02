package com.project.docmind.docmind.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.docmind.docmind.dto.LoginRequest;
import com.project.docmind.docmind.dto.LoginResponse;
import com.project.docmind.docmind.dto.RegisterUserRequest;
import com.project.docmind.docmind.dto.UserDto;
import com.project.docmind.docmind.entity.User;
import com.project.docmind.docmind.repository.UserRepository;
import com.project.docmind.docmind.service.CustomUserDetail;
import com.project.docmind.docmind.service.JwtService;
import com.project.docmind.docmind.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final JwtService jwtService;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
		Authentication authentication=new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
		Authentication authenticated=authenticationManager.authenticate(authentication);
		User user= userRepository.findByUsername(loginRequest.username()).orElseThrow(() -> new RuntimeException("User not found"));
		String token=jwtService.generateToken(new CustomUserDetail(user));
		var response=new LoginResponse(token,new UserDto(user.getId(),user.getUsername(),user.getEmail(),user.getRole()));
		return ResponseEntity.ok(response);
	}

	@PostMapping("/register")
	public ResponseEntity<UserDto> register(@RequestBody RegisterUserRequest registerUserRequest){
		UserDto userDto=userService.registerUser(registerUserRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
	}

}
