package boardexample.myboard.domain.member.controller;

import boardexample.myboard.domain.member.dto.*;
import boardexample.myboard.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입
     */
    @PostMapping("/signUp")
    @ResponseStatus(HttpStatus.OK)
    public void signUp(@RequestBody MemberSignUpDto memberSignUpDto) throws Exception {
        memberService.signUp(memberSignUpDto);
    }

    /**
     * 회원정보수정
     */
    @PutMapping("/member")
    @ResponseStatus(HttpStatus.OK)
    public void updateBasicInfo(@RequestBody MemberUpdateDto memberUpdateDto) throws Exception {
        memberService.update(memberUpdateDto);
    }

    /**
     * 비밀번호 수정
     */
    @PutMapping("/member/password")
    @ResponseStatus(HttpStatus.OK)
    public void updatePassword(@RequestBody UpdatePasswordDto updatePasswordDto) throws Exception {
        memberService.updatePassword(updatePasswordDto.checkPassword(),updatePasswordDto.toBePassword());
    }


    /**
     * 회원탈퇴
     */
    @DeleteMapping("/member")
    @ResponseStatus(HttpStatus.OK)
    public void withdraw(@RequestBody MemberWithdrawDto memberWithdrawDto) throws Exception {
        memberService.withdraw(memberWithdrawDto.checkPassword());
    }


    /**
     * 회원정보조회
     */
    @GetMapping("/member/{id}")
    public ResponseEntity getInfo(@PathVariable("id") Long id) throws Exception {
        MemberInfoDto info = memberService.getInfo(id);
        return new ResponseEntity(info, HttpStatus.OK);
    }

    /**
     * 내정보조회
     */
    @GetMapping("/member")
    public ResponseEntity getMyInfo() throws Exception {
        MemberInfoDto info = memberService.getMyInfo();
        return new ResponseEntity(info, HttpStatus.OK);
    }



}
