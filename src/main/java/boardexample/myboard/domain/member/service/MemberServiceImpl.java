package boardexample.myboard.domain.member.service;

import boardexample.myboard.domain.member.Member;
import boardexample.myboard.domain.member.dto.MemberInfoDto;
import boardexample.myboard.domain.member.dto.MemberSignUpDto;
import boardexample.myboard.domain.member.dto.MemberUpdateDto;
import boardexample.myboard.domain.member.exception.MemberException;
import boardexample.myboard.domain.member.exception.MemberExceptionType;
import boardexample.myboard.domain.member.repository.MemberRepository;
import boardexample.myboard.global.cache.CacheLogin;
import boardexample.myboard.global.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService{


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * 회원가입
     */
    @Override
    public void signUp(MemberSignUpDto memberSignUpDto) throws Exception {

        Member member = memberSignUpDto.toEntity();


        //Member에 USER 권한 부여
        member.addUserAuthority();

        //회원가입 시 입력받은 비밀번호 암호화
        member.encodePassword(passwordEncoder);



        /**
         * 이미 존재하는 아이디로 회원가입 요청 시 -> 예외 발생
         */

        if(memberRepository.findByUsername(memberSignUpDto.username()).isPresent()){
            throw new MemberException(MemberExceptionType.ALREADY_EXIST_USERNAME);
        }



        //회원가입 완료
        memberRepository.save(member);
    }



    /**
     * 회원정보 수정
     */
    @Override
    @CacheEvict(value = CacheLogin.USER, key = "#p1")
    public void update(MemberUpdateDto memberUpdateDto, String username) throws Exception {
        Member member = memberRepository.findByUsername(
                        username) //SecurityContextHolder에 들어있는 Username 가져옴, TODO : 이거 변경함
                                        .orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));


        memberUpdateDto.age().ifPresent(member::updateAge);
        memberUpdateDto.name().ifPresent(member::updateName);
        memberUpdateDto.nickName().ifPresent(member::updateNickName);
    }


    /**
     * 비밀번호 변경
     */
    @Override
    @CacheEvict(value = CacheLogin.USER, key = "#p1")
    public void updatePassword(String asIsPassword, String toBePassword, String username) throws Exception {


        Member member = memberRepository.findByUsername(
                        username)
                                        .orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));



        if(!member.matchPassword(passwordEncoder, asIsPassword) ) {
            throw new MemberException(MemberExceptionType.WRONG_PASSWORD);
        }



        member.updatePassword(passwordEncoder, toBePassword);
    }


    /**
     * 회원탈퇴
     */
    @Override
    @CacheEvict(value = CacheLogin.USER, key = "#p1")
    public void withdraw(String checkPassword, String username) throws Exception {
        Member member = memberRepository.findByUsername(
                        username)
                                        .orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));


        if(!member.matchPassword(passwordEncoder, checkPassword) ) {
            throw new MemberException(MemberExceptionType.WRONG_PASSWORD);
        }


        memberRepository.delete(member);
    }


    /**
     * 회원정보 가져오기
     */
    @Override
    public MemberInfoDto getInfo(Long id) throws Exception {
        Member findMember = memberRepository.findById(id).orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));
        return new MemberInfoDto(findMember);
    }

    /**
     * 내정보 가져오기
     */
    @Override
    public MemberInfoDto getMyInfo() throws Exception {
        Member findMember = memberRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));
        return new MemberInfoDto(findMember);
    }
}
