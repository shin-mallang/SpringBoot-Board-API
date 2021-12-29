package boardexample.myboard.domain.member.repository;

import boardexample.myboard.domain.member.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {


    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;



    private void clear(){
        em.flush();
        em.clear();
    }

    @AfterEach
    private void after(){
        em.clear();
    }

    @Test
    public void 회원저장_성공() throws Exception {
        //given
        Member member = Member.builder().username("username").password("1234567890").name("Member1").nickName("NickName1").age(22).build();

        //when
        Member saveMember = memberRepository.save(member);

        //then
        Member findMember = memberRepository.findById(saveMember.getId()).orElseThrow(() -> new RuntimeException("저장된 회원이 없습니다"));//아직 예외 클래스를 만들지 않았기에 RuntimeException으로 처리하겠습니다.

        assertThat(findMember).isSameAs(saveMember);
        assertThat(findMember).isSameAs(member);
    }

    @Test
    public void 오류_회원가입시_아이디가_없음() throws Exception {
        //given
        Member member = Member.builder().password("1234567890").name("Member1").nickName("NickName1").age(22).build();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member));
    }

    @Test
    public void 오류_회원가입시_이름이_없음() throws Exception {
        //given
        Member member = Member.builder().username("username").password("1234567890").nickName("NickName1").age(22).build();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member));
    }

    @Test
    public void 오류_회원가입시_닉네임이_없음() throws Exception {
        //given
        Member member = Member.builder().username("username").password("1234567890").name("Member1").age(22).build();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member));
    }

    @Test
    public void 오류_회원가입시_나이가_없음() throws Exception {
        //given
        Member member = Member.builder().username("username").password("1234567890").name("Member1").nickName("NickName1").build();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member));
    }

    @Test
    public void 오류_회원가입시_중복된_아이디가_있음() throws Exception {
        //given
        Member member1 = Member.builder().username("username").password("1234567890").name("Member1").nickName("NickName1").age(22).build();
        Member member2 = Member.builder().username("username").password("1111111111").name("Member2").nickName("NickName2").age(22).build();

        memberRepository.save(member1);
        clear();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member2));

    }

    @Test
    public void 성공_회원수정() throws Exception {
        //given
        Member member1 = Member.builder().username("username").password("1234567890").name("Member1").nickName("NickName1").age(22).build();
        memberRepository.save(member1);
        clear();

        String updatePassword = "updatePassword";
        String updateName = "updateName";
        String updateNickName = "updateNickName";
        int updateAge = 33;

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        //when
        Member findMember = memberRepository.findById(member1.getId()).orElseThrow(() -> new Exception());
        findMember.updateAge(updateAge);
        findMember.updateName(updateName);
        findMember.updateNickName(updateNickName);
        findMember.updatePassword(passwordEncoder,updatePassword);
        em.flush();

        //then
        Member findUpdateMember = memberRepository.findById(findMember.getId()).orElseThrow(() -> new Exception());

        assertThat(findUpdateMember).isSameAs(findMember);
        assertThat(passwordEncoder.matches(updatePassword, findUpdateMember.getPassword())).isTrue();
        assertThat(findUpdateMember.getName()).isEqualTo(updateName);
        assertThat(findUpdateMember.getName()).isNotEqualTo(member1.getName());
    }

    @Test
    public void 성공_회원삭제() throws Exception {
        //given
        Member member1 = Member.builder().username("username").password("1234567890").name("Member1").nickName("NickName1").age(22).build();
        memberRepository.save(member1);
        clear();

        //when
        memberRepository.delete(member1);
        clear();

        //then
        assertThrows(Exception.class,
                () -> memberRepository.findById(member1.getId())
                        .orElseThrow(() -> new Exception()));
    }


    @Test
    public void existByUsername_정상작동() throws Exception {
        //given
        String username = "username";
        Member member1 = Member.builder().username(username).password("1234567890").name("Member1").nickName("NickName1").age(22).build();
        memberRepository.save(member1);
        clear();

        //when, then
        assertThat(memberRepository.existsByUsername(username)).isTrue();
        assertThat(memberRepository.existsByUsername(username+"123")).isFalse();

    }


    @Test
    public void findByUsername_정상작동() throws Exception {
        //given
        String username = "username";
        Member member1 = Member.builder().username(username).password("1234567890").name("Member1").nickName("NickName1").age(22).build();
        memberRepository.save(member1);
        clear();


        //when, then
        assertThat(memberRepository.findByUsername(username)).isEqualTo(member1);
        assertThrows(Exception.class,
                () -> memberRepository.findByUsername(username+"123")
                        .orElseThrow(() -> new Exception()));

    }

    @Test
    public void 회원가입시_생성시간_등록() throws Exception {
        //given
        Member member1 = Member.builder().username("username").password("1234567890").name("Member1").nickName("NickName1").age(22).build();
        memberRepository.save(member1);
        clear();

        //when
        Member findMember = memberRepository.findById(member1.getId()).orElseThrow(() -> new Exception());

        //then
        assertThat(findMember.getCreatedDate()).isNotNull();
        assertThat(findMember.getLastModifiedDate()).isNotNull();

    }


}