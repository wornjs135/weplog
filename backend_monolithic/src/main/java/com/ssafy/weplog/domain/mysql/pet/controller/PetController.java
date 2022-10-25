package com.ssafy.weplog.domain.mysql.pet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.weplog.global.config.security.auth.CustomUserDetails;
import com.ssafy.weplog.domain.mysql.pet.service.PetService;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pet")
public class PetController {

	private final PetService petService;

	@ApiOperation(value = "나의펫 조회하기")
	@GetMapping("/mypet")
	public ResponseEntity<?> getMyPets(@ApiIgnore @AuthenticationPrincipal CustomUserDetails member) {
		return ResponseEntity.ok(petService.getMyPets(member.getId()));
	}


	@ApiOperation(value = "레벨별 펫 조회")
	@GetMapping("/level/{level}")
	public ResponseEntity<?> getPetsByLevel(@PathVariable("level") int level) {
		return ResponseEntity.ok(petService.getPetsByLevel(level));
	}




}
