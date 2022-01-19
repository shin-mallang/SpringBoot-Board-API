package boardexample.myboard.domain.post.service;

import boardexample.myboard.domain.member.Role;
import boardexample.myboard.domain.member.dto.MemberSignUpDto;
import boardexample.myboard.domain.member.service.MemberService;
import boardexample.myboard.domain.post.Post;
import boardexample.myboard.domain.post.dto.PostSaveDto;
import boardexample.myboard.domain.post.dto.PostUpdateDto;
import boardexample.myboard.domain.post.exception.PostException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Transactional
class PostServiceImplTest {


    @Autowired private EntityManager em;

    @Autowired
    private PostService postService;

    @Autowired
    private MemberService memberService;

    private static final String USERNAME = "username";
    private static final String PASSWORD = "PASSWORD123@@@";

    private String title = "제목";
    private String content = "내용";




    private void clear(){
        em.flush();
        em.clear();
    }

    private void deleteFile(String filePath) {
        File files = new File(filePath);
        files.delete();
    }

    private MockMultipartFile getMockUploadFile() throws IOException {
        return new MockMultipartFile("file", "file.jpg", "image/jpg", new FileInputStream("C:/Users/user/Desktop/tistory/diary.jpg"));
    }

    private Post findPost() {
        return em.createQuery("select p from Post p", Post.class).getSingleResult();
    }


    @BeforeEach
    private void signUpAndSetAuthentication() throws Exception {
        memberService.signUp(new MemberSignUpDto(USERNAME,PASSWORD,"name","nickName",22));
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username(USERNAME)
                                .password(PASSWORD)
                                .roles(Role.USER.toString())
                                .build(),
                        null)
        );
        SecurityContextHolder.setContext(emptyContext);
        clear();
    }




    @Test
    public void 포스트_저장_성공_업로드_파일_없음() throws Exception {
        //given
        PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());


        //when
        postService.save(postSaveDto);
        clear();


        //then
        Post findPost = findPost();
        Post post = em.find(Post.class, findPost.getId());
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
        assertThat(post.getFilePath()).isNull();
    }





    @Test
    public void 포스트_저장_성공_업로드_파일_있음() throws Exception {
        //given
        PostSaveDto postSaveDto = new PostSaveDto(title,content, Optional.ofNullable(getMockUploadFile()));

        //when
        postService.save(postSaveDto);
        clear();

        //then
        Post findPost = findPost();
        Post post = em.find(Post.class, findPost.getId());
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
        assertThat(post.getFilePath()).isNotNull();

        deleteFile(post.getFilePath());
        //올린 파일 삭제
    }




    @Test
    public void 포스트_저장_실패_제목이나_내용이_없음() throws Exception {
        //given
        PostSaveDto postSaveDto = new PostSaveDto(null,content, Optional.empty());
        PostSaveDto postSaveDto2 = new PostSaveDto(title,null, Optional.empty());


        //when,then
        assertThrows(Exception.class, () -> postService.save(postSaveDto));
        assertThrows(Exception.class, () -> postService.save(postSaveDto2));

    }




    @Test
    public void 포스트_업데이트_성공_업로드파일_없음TO없음() throws Exception {
        //given
        PostSaveDto postSaveDto = new PostSaveDto(title,content, Optional.empty());
        postService.save(postSaveDto);
        clear();

        //when
        Post findPost = findPost();
        PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"),Optional.ofNullable("바꾼내용"), Optional.empty());
        postService.update(findPost.getId(),postUpdateDto);
        clear();

        //then
        Post post = em.find(Post.class, findPost.getId());
        assertThat(post.getContent()).isEqualTo("바꾼내용");
        assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
        assertThat(post.getFilePath()).isNull();

    }



    @Test
    public void 포스트_업데이트_성공_업로드파일_없음TO있음() throws Exception {
        //given
        PostSaveDto postSaveDto = new PostSaveDto(title,content, Optional.empty());
        postService.save(postSaveDto);
        clear();


        //when
        Post findPost = findPost();

        PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"),Optional.ofNullable("바꾼내용"), Optional.ofNullable(getMockUploadFile()));
        postService.update(findPost.getId(),postUpdateDto);
        clear();


        //then
        Post post = em.find(Post.class, findPost.getId());
        assertThat(post.getContent()).isEqualTo("바꾼내용");
        assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
        assertThat(post.getFilePath()).isNotNull();

        deleteFile(post.getFilePath());
        //올린 파일 삭제
    }




    @Test
    public void 포스트_업데이트_성공_업로드파일_있음TO없음() throws Exception {
        //given
        PostSaveDto postSaveDto = new PostSaveDto(title,content, Optional.ofNullable(getMockUploadFile()));
        postService.save(postSaveDto);

        Post findPost = findPost();
        assertThat(findPost.getFilePath()).isNotNull();
        clear();

        //when
        PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"),Optional.ofNullable("바꾼내용"), Optional.empty());
        postService.update(findPost.getId(),postUpdateDto);
        clear();

        //then
        findPost = em.find(Post.class, findPost.getId());
        assertThat(findPost.getContent()).isEqualTo("바꾼내용");
        assertThat(findPost.getWriter().getUsername()).isEqualTo(USERNAME);
        assertThat(findPost.getFilePath()).isNull();
    }




    @Test
    public void 포스트_업데이트_성공_업로드파일_있음TO있음() throws Exception {
        //given
        PostSaveDto postSaveDto = new PostSaveDto(title,content, Optional.empty());
        postService.save(postSaveDto);

        Post findPost = findPost();
        Post post = em.find(Post.class, findPost.getId());
        String filePath = post.getFilePath();
        clear();

        //when
        PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"),Optional.ofNullable("바꾼내용"), Optional.ofNullable(getMockUploadFile()));
        postService.update(findPost.getId(),postUpdateDto);
        clear();

        //then
        post = em.find(Post.class, findPost.getId());
        assertThat(post.getContent()).isEqualTo("바꾼내용");
        assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
        assertThat(post.getFilePath()).isNotEqualTo(filePath);
        deleteFile(post.getFilePath());
        //올린 파일 삭제
    }




    private void setAnotherAuthentication() throws Exception {
        memberService.signUp(new MemberSignUpDto(USERNAME+"123",PASSWORD,"name","nickName",22));
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username(USERNAME+"123")
                                .password(PASSWORD)
                                .roles(Role.USER.toString())
                                .build(),
                        null)
        );
        SecurityContextHolder.setContext(emptyContext);
        clear();
    }


    @Test
    public void 포스트_업데이트_실패_권한이없음() throws Exception {
        PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());

        postService.save(postSaveDto);
        clear();


        //when, then
        setAnotherAuthentication();
        Post findPost = findPost();
        PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"),Optional.ofNullable("바꾼내용"), Optional.empty());


        assertThrows(PostException.class, ()-> postService.update(findPost.getId(),postUpdateDto));

    }




    @Test
    public void 포스트삭제_성공() throws Exception {
        PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());
        postService.save(postSaveDto);
        clear();

        //when
        Post findPost = findPost();
        postService.delete(findPost.getId());

        //then
        List<Post> findPosts = em.createQuery("select p from Post p", Post.class).getResultList();
        assertThat(findPosts.size()).isEqualTo(0);
    }



    @Test
    public void 포스트삭제_실패() throws Exception {
        PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());

        postService.save(postSaveDto);
        clear();

        //when, then
        setAnotherAuthentication();
        Post findPost = findPost();
        assertThrows(PostException.class, ()-> postService.delete(findPost.getId()));
    }


}