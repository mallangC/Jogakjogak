package com.zb.jogakjogak.security.controller;


import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.dto.MemberResponseDto;
import com.zb.jogakjogak.security.dto.UpdateMemberRequestDto;
import com.zb.jogakjogak.security.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "회원의 상세정보에 관한 API", description = "회원의 상세정보를 조회하고 수정할 수 있는 API")
@RequestMapping("/member/my-page")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 상세정보 조회", description = "로그인된 회원의 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<HttpApiResponse<MemberResponseDto>> getMember(@AuthenticationPrincipal CustomOAuth2User customOAuth2User){

        String username = customOAuth2User.getName();

        MemberResponseDto memberResponseDto = memberService.getMember(username);
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(memberResponseDto,
                                "회원정보 조회 완료",
                                HttpStatus.OK)
                );
    }

    @Operation(summary = "회원 상세정보 수정", description = "로그인된 회원의 정보를 수정합니다.")
    @PatchMapping("/update")
    public ResponseEntity<HttpApiResponse<MemberResponseDto>> updateMember(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                                                           @Valid @RequestBody UpdateMemberRequestDto updateMemberRequestDto){
        String username = customOAuth2User.getName();

        MemberResponseDto memberResponseDto = memberService.updateMember(username, updateMemberRequestDto);
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(memberResponseDto,
                                "회원정보 수정 완료",
                                HttpStatus.OK)
                );
    }
}
