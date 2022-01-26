package boardexample.myboard.domain.post.controller;

import boardexample.myboard.domain.member.Member;
import boardexample.myboard.domain.member.Role;
import boardexample.myboard.domain.member.repository.MemberRepository;
import boardexample.myboard.domain.member.service.MemberService;
import boardexample.myboard.domain.post.Post;
import boardexample.myboard.domain.post.repository.PostRepository;
import boardexample.myboard.domain.post.service.PostService;
import boardexample.myboard.global.jwt.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;

    @Autowired MemberRepository memberRepository;
    @Autowired PostRepository postRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService;
    final String USERNAME = "username1";

    private static  Member member;


    private void clear() {
        em.flush();
        em.clear();
    }

    @BeforeEach
    public void signUpMember(){
        member = memberRepository.save(Member.builder().username(USERNAME).password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이1").role(Role.USER).age(22).build());
        clear();
    }

    private String getAccessToken(){
        return jwtService.createAccessToken(USERNAME);
    }


    private MockMultipartFile getMockUploadFile() throws IOException {
        //TODO : name이 중요
        return new MockMultipartFile("uploadFile", "file.jpg", "image/jpg", new FileInputStream("C:/Users/user/Desktop/tistory/diary.jpg"));
    }

    /**
     * 게시글 저장
     */
    @Test
    public void 게시글_저장_성공() throws Exception {
        //given
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "제목");
        map.add("content", "내용");


        //when
        mockMvc.perform(
                post("/post")
                        .header("Authorization", "Bearer "+ getAccessToken())
                .contentType(MediaType.MULTIPART_FORM_DATA).params(map))
                .andExpect(status().isCreated());


        //then
        Assertions.assertThat(postRepository.findAll().size()).isEqualTo(1);
    }

    /**
     * 게시글 저장
     */
    @Test
    public void 게시글_저장_실패_제목이나_내용이_없음() throws Exception {
        //given

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "제목");


        //when, then
        mockMvc.perform(
                        post("/post")
                                .header("Authorization", "Bearer "+ getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isBadRequest());

        map = new LinkedMultiValueMap<>();
        map.add("content", "내용");
        mockMvc.perform(
                        post("/post")
                                .header("Authorization", "Bearer "+ getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isBadRequest());

    }




    /**
     * 게시글 수정
     */
    @Test
    public void 게시글_수정_제목변경_성공() throws Exception {
        //given
        Post post = Post.builder().title("수정전제목").content("수정전내용").build();
        post.confirmWriter(member);
        Post savePost = postRepository.save(post);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_TITLE = "제목";
        map.add("title", UPDATE_TITLE);

        //when
        mockMvc.perform(
                        put("/post/"+savePost.getId())
                                .header("Authorization", "Bearer "+ getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))

                .andExpect(status().isOk());


        //then
        Assertions.assertThat(postRepository.findAll().get(0).getTitle()).isEqualTo(UPDATE_TITLE);
    }

    /**
     * 게시글 수정
     */
    @Test
    public void 게시글_수정_내용변경_성공() throws Exception {
        //given
        Post post = Post.builder().title("수정전제목").content("수정전내용").build();
        post.confirmWriter(member);
        Post savePost = postRepository.save(post);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_CONTENT = "내용";
        map.add("content", UPDATE_CONTENT);

        //when
        mockMvc.perform(
                        put("/post/"+savePost.getId())
                                .header("Authorization", "Bearer "+ getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isOk());


        //then
        Assertions.assertThat(postRepository.findAll().get(0).getContent()).isEqualTo(UPDATE_CONTENT);
    }



    /**
     * 게시글 수정
     */
    @Test
    public void 게시글_수정_제목내용변경_성공() throws Exception {
        //given
        Post post = Post.builder().title("수정전제목").content("수정전내용").build();
        post.confirmWriter(member);
        Post savePost = postRepository.save(post);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_CONTENT = "내용";
        final String UPDATE_TITlE = "제목";
        map.add("title", UPDATE_TITlE);
        map.add("content", UPDATE_CONTENT);

        //when
        mockMvc.perform(
                        put("/post/"+savePost.getId())
                                .header("Authorization", "Bearer "+ getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isOk());


        //then
        Assertions.assertThat(postRepository.findAll().get(0).getContent()).isEqualTo(UPDATE_CONTENT);
        Assertions.assertThat(postRepository.findAll().get(0).getTitle()).isEqualTo(UPDATE_TITlE);
    }

    /**
      게시글 수정
     */
    @Test
    public void 게시글_수정_업로드파일추가_성공() throws Exception {
        //given
        Post post = Post.builder().title("수정전제목").content("수정전내용").build();
        post.confirmWriter(member);
        Post savePost = postRepository.save(post);

        MockMultipartFile mockUploadFile = getMockUploadFile();


        //when

        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/post/" + savePost.getId());
        requestBuilder.with(request -> {
            request.setMethod(HttpMethod.PUT.name());
            return request;
        });

        mockMvc.perform(requestBuilder
                        .file(getMockUploadFile())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isOk());

        /*mockMvc.perform(multipart("/post/"+savePost.getId())

                                .file(getMockUploadFile())
                                .header("Authorization", "Bearer "+ getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                               )
                .andExpect(status().isOk());
*/

        //then
        Assertions.assertThat(postRepository.findAll().get(0).getFilePath()).isNotNull();
    }



    /**
     * 게시글 수정
     */
    @Test
    public void 게시글_수정_실패_권한없음() throws Exception {
        //given
        Member newMember = memberRepository.save(Member.builder().username("newMEmber1123").password("!23123124421").name("123213").nickName("123").age(22).role(Role.USER).build());
        Post post = Post.builder().title("수정전제목").content("수정전내용").build();
        post.confirmWriter(newMember);
        Post savePost = postRepository.save(post);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_CONTENT = "내용";
        final String UPDATE_TITlE = "제목";
        map.add("title", UPDATE_TITlE);
        map.add("content", UPDATE_CONTENT);

        //when
        mockMvc.perform(
                        put("/post/"+savePost.getId())
                                .header("Authorization", "Bearer "+ getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isForbidden());


        //then
        Assertions.assertThat(postRepository.findAll().get(0).getContent()).isEqualTo("수정전내용");
        Assertions.assertThat(postRepository.findAll().get(0).getTitle()).isEqualTo("수정전제목");
    }



    /**
     * 게시글 삭제
     */
    @Test
    public void 게시글_삭제_성공() throws Exception {
        //given
        Post post = Post.builder().title("수정전제목").content("수정전내용").build();
        post.confirmWriter(member);
        Post savePost = postRepository.save(post);

        //when
        mockMvc.perform(
                        delete("/post/"+savePost.getId())
                                .header("Authorization", "Bearer "+ getAccessToken())
                ).andExpect(status().isOk());


        //then
        Assertions.assertThat(postRepository.findAll().size()).isEqualTo(0);

    }

    /**
     * 게시글 삭제
     */
    @Test
    public void 게시글_삭제_실패_권한없음() throws Exception {
        //given
        Member newMember = memberRepository.save(Member.builder().username("newMEmber1123").password("!23123124421").name("123213").nickName("123").age(22).role(Role.USER).build());
        Post post = Post.builder().title("수정전제목").content("수정전내용").build();
        post.confirmWriter(newMember);
        Post savePost = postRepository.save(post);

        //when
        mockMvc.perform(
                delete("/post/"+savePost.getId())
                        .header("Authorization", "Bearer "+ getAccessToken())
        ).andExpect(status().isForbidden());


        //then
        Assertions.assertThat(postRepository.findAll().size()).isEqualTo(1);

    }



    /**
     * 게시글 조회
     */
    @Test
    public void 게시글_조회() throws Exception {


    }



    /**
     * 게시글 검색
     */

}