package boardexample.myboard.domain.member.controller;

import boardexample.myboard.domain.member.Member;
import boardexample.myboard.domain.member.dto.MemberSignUpDto;
import boardexample.myboard.domain.member.dto.MemberUpdateDto;
import boardexample.myboard.domain.member.repository.MemberRepository;
import boardexample.myboard.domain.member.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {


    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    ObjectMapper objectMapper = new ObjectMapper();

    private static String SIGN_UP_URL = "/signUp";

    private String username = "username";
    private String password = "password1234@";
    private String name = "신동훈";
    private String nickName = "밥 잘먹는 동훈이";
    private Integer age = 22;





    private void clear(){
        em.flush();
        em.clear();
    }

    private void signUp(String signUpData) throws Exception {
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(signUpData))
                .andExpect(status().isOk());
    }


    @Value("${jwt.access.header}")
    private String accessHeader;
    private static final String BEARER = "Bearer ";

    private String getAccessToken() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("username",username);
        map.put("password",password);


        MvcResult result = mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(map)))
                .andExpect(status().isOk()).andReturn();

        return result.getResponse().getHeader(accessHeader);
    }


    @Test
    public void 회원가입_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));

        //when
        signUp(signUpData);

        //then
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(member.getName()).isEqualTo(name);
        assertThat(memberRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    public void 회원가입_실패_필드가_없음() throws Exception {
        //given
        String noUsernameSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(null, password, name, nickName, age));
        String noPasswordSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, null, name, nickName, age));
        String noNameSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, null, nickName, age));
        String noNickNameSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, null, age));
        String noAgeSignUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, null));

        //when, then
        signUp(noUsernameSignUpData);//예외가 발생하더라도 상태코드는 200
        signUp(noPasswordSignUpData);//예외가 발생하더라도 상태코드는 200
        signUp(noNameSignUpData);//예외가 발생하더라도 상태코드는 200
        signUp(noNickNameSignUpData);//예외가 발생하더라도 상태코드는 200
        signUp(noAgeSignUpData);//예외가 발생하더라도 상태코드는 200

        assertThat(memberRepository.findAll().size()).isEqualTo(0);
    }


    @Test
    public void 회원정보수정_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));

        signUp(signUpData);

        String accessToken = getAccessToken();
        Map<String, Object> map = new HashMap<>();
        map.put("name",name+"변경");
        map.put("nickName",nickName+"변경");
        map.put("age",age+1);
        String updateMemberData = objectMapper.writeValueAsString(map);


        //when
        mockMvc.perform(
                        put("/member")
                                .header(accessHeader,BEARER+accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateMemberData))
                .andExpect(status().isOk());

        //then
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(member.getName()).isEqualTo(name+"변경");
        assertThat(member.getNickName()).isEqualTo(nickName+"변경");
        assertThat(member.getAge()).isEqualTo(age+1);
        assertThat(memberRepository.findAll().size()).isEqualTo(1);

    }

    @Test
    public void 회원정보수정_원하는필드만변경_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(username, password, name, nickName, age));

        signUp(signUpData);

        String accessToken = getAccessToken();
        Map<String, Object> map = new HashMap<>();
        map.put("name",name+"변경");
        String updateMemberData = objectMapper.writeValueAsString(map);


        //when
        mockMvc.perform(
                        put("/member")
                                .header(accessHeader,BEARER+accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateMemberData))
                .andExpect(status().isOk());

        //then
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(member.getName()).isEqualTo(name+"변경");
        assertThat(member.getNickName()).isEqualTo(nickName);
        assertThat(member.getAge()).isEqualTo(age);
        assertThat(memberRepository.findAll().size()).isEqualTo(1);

    }

    /**
     * 비밀번호수정, 성공
     * 비빌번호수정, 실패
     * 회원탈퇴 성공
     * 회원탈퇴 실패
     * 내정보조회 성공
     * 내정보조회 실패(로그인안함)
     * 회원정보조회 성공
     * 회원정보조회 실패 -> 회원이없음
     */




}