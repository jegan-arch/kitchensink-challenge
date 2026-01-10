package com.modern.member.controller;

import com.modern.member.dto.ChangePasswordRequest;
import com.modern.member.dto.MemberRequest;
import com.modern.member.dto.MemberResponse;
import com.modern.member.dto.MessageResponse;
import com.modern.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable String id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MemberResponse> getMyProfile() {
        return ResponseEntity.ok(memberService.getMyProfile());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MemberResponse> updateProfile(@PathVariable String id,
                                                        @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteMember(@PathVariable String id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(new MessageResponse("Member deleted successfully."));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateUserRole(@PathVariable String id, @RequestBody Map<String, String> payload) {
        String role = payload.get("role");
        memberService.updateMemberRole(id, role);
        return ResponseEntity.ok(new MessageResponse("Role updated and session invalidated."));
    }

    @PutMapping("/{id}/change-password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageResponse> changePassword(@PathVariable String id,
                                                          @RequestBody @Valid ChangePasswordRequest request) {
        memberService.changePassword(id, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully."));
    }
}