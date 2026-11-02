package com.project.docmind.docmind.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project.docmind.docmind.repository.UserRepository;
import com.project.docmind.docmind.service.CustomUserDetail;
import com.project.docmind.docmind.service.CustomUserDetailService;
import com.project.docmind.docmind.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final CustomUserDetailService customUserDetailService;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader=request.getHeader("Authorization");
		if(authHeader==null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token=authHeader.substring(7);
		String username;
		try {
			username=jwtService.extractUsername(token);
			if(username==null || !jwtService.isTokenValid(token, customUserDetailService.loadUserByUsername(username))) {
				filterChain.doFilter(request, response);
				return;
			}
			var user=userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
			var authentication=new UsernamePasswordAuthenticationToken(user,null,new CustomUserDetail(user).getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);

		}catch(Exception e) {
			filterChain.doFilter(request, response);
			e.printStackTrace();
			return;
		}
		filterChain.doFilter(request, response);
	}

}
