package com.project.docmind.docmind.dto;

import com.project.docmind.docmind.entity.Role;

public record UserDto(
		Long id,
		String username,
		String email,
		Role role
		) {

}
