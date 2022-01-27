package boardexample.myboard.domain.post.service;

import boardexample.myboard.domain.commnet.Comment;
import boardexample.myboard.domain.commnet.dto.CommentInfoDto;
import boardexample.myboard.domain.commnet.repository.CommentRepository;
import boardexample.myboard.domain.member.Member;
import boardexample.myboard.domain.member.Role;
import boardexample.myboard.domain.member.dto.MemberSignUpDto;
import boardexample.myboard.domain.member.repository.MemberRepository;
import boardexample.myboard.domain.member.service.MemberService;
import boardexample.myboard.domain.post.Post;
import boardexample.myboard.domain.post.cond.PostSearchCondition;
import boardexample.myboard.domain.post.dto.PostInfoDto;
import boardexample.myboard.domain.post.dto.PostPagingDto;
import boardexample.myboard.domain.post.dto.PostSaveDto;
import boardexample.myboard.domain.post.dto.PostUpdateDto;
import boardexample.myboard.domain.post.exception.PostException;
import boardexample.myboard.domain.post.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;


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


    @Test
    public void 포스트_조회() throws Exception {
        Member member1 = memberRepository.save(Member.builder().username("username1").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이1").role(Role.USER).age(22).build());
        Member member2 = memberRepository.save(Member.builder().username("username2").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이2").role(Role.USER).age(22).build());
        Member member3 = memberRepository.save(Member.builder().username("username3").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이3").role(Role.USER).age(22).build());
        Member member4 = memberRepository.save(Member.builder().username("username4").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이4").role(Role.USER).age(22).build());
        Member member5 = memberRepository.save(Member.builder().username("username5").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이5").role(Role.USER).age(22).build());

        Map<Integer, Long> memberIdMap = new HashMap<>();
        memberIdMap.put(1,member1.getId());
        memberIdMap.put(2,member2.getId());
        memberIdMap.put(3,member4.getId());
        memberIdMap.put(4,member4.getId());
        memberIdMap.put(5,member5.getId());



        /**
         * Post 생성
         */

        Post post = Post.builder().title("게시글").content("내용").build();
        post.confirmWriter(member1);
        postRepository.save(post);
        em.flush();


        /**
         * Comment 생성(댓글)
         */

        final int COMMENT_COUNT = 10;

        for(int i = 1; i<=COMMENT_COUNT; i++ ){
            Comment comment = Comment.builder().content("댓글" + i).build();
            comment.confirmWriter(memberRepository.findById(memberIdMap.get(i % 3 + 1)).orElse(null));
            comment.confirmPost(post);
            commentRepository.save(comment);
        }






        /**
         * ReComment 생성(대댓글)
         */
        final int COMMENT_PER_RECOMMENT_COUNT = 20;
        commentRepository.findAll().stream().forEach(comment -> {

            for(int i = 1; i<=20; i++ ){
                Comment recomment = Comment.builder().content("대댓글" + i).build();
                recomment.confirmWriter(memberRepository.findById(memberIdMap.get(i % 3 + 1)).orElse(null));

                recomment.confirmPost(comment.getPost());
                recomment.confirmParent(comment);
                commentRepository.save(recomment);
            }

        });



        clear();




        //when
        PostInfoDto postInfo = postService.getPostInfo(post.getId());



        //then
        assertThat(postInfo.getPostId()).isEqualTo(post.getId());
        assertThat(postInfo.getContent()).isEqualTo(post.getContent());
        assertThat(postInfo.getWriterDto().getUsername()).isEqualTo(post.getWriter().getUsername());


        int recommentCount = 0;
        for (CommentInfoDto commentInfoDto : postInfo.getCommentInfoDtoList()) {
            recommentCount += commentInfoDto.getReCommentListDtoList().size();
        }

        assertThat(postInfo.getCommentInfoDtoList().size()).isEqualTo(COMMENT_COUNT);
        assertThat(recommentCount).isEqualTo(COMMENT_PER_RECOMMENT_COUNT * COMMENT_COUNT);

    }

    @Test
    public void 포스트_검색_조건없음() throws Exception {
        //given

        /**
         * MEMBER 저장
         */

        Member member1 = memberRepository.save(Member.builder().username("username1").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이1").role(Role.USER).age(22).build());



        /**
         * Post 생성
         */
        final int POST_COUNT = 50;
        for(int i = 1; i<= POST_COUNT; i++ ){
            Post post = Post.builder().title("게시글"+ i).content("내용"+i).build();
            post.confirmWriter(member1);
            postRepository.save(post);
        }

        clear();



        //when
        final int PAGE = 0;
        final int SIZE = 20;
        PageRequest pageRequest = PageRequest.of(PAGE, SIZE);

        PostSearchCondition postSearchCondition = new PostSearchCondition();

        PostPagingDto postList = postService.getPostList(pageRequest, postSearchCondition);


        //then
        assertThat(postList.getTotalElementCount()).isEqualTo(POST_COUNT);

        assertThat(postList.getTotalPageCount()).isEqualTo((POST_COUNT % SIZE == 0)
                                                                        ? POST_COUNT/SIZE
                                                                        : POST_COUNT/SIZE + 1);

        assertThat(postList.getCurrentPageNum()).isEqualTo(PAGE);
        assertThat(postList.getCurrentPageElementCount()).isEqualTo(SIZE);
    }

    @Test
    public void 포스트_검색_제목일치() throws Exception {
        //given
        /**
         * MEMBER 저장
         */

        Member member1 = memberRepository.save(Member.builder().username("username1").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이1").role(Role.USER).age(22).build());


        /**
         * 일반 POST 생성
         */
        final int DEFAULT_POST_COUNT = 100;
        for(int i = 1; i<=DEFAULT_POST_COUNT; i++ ){
            Post post = Post.builder().title("게시글"+ i).content("내용"+i).build();
            post.confirmWriter(member1);
            postRepository.save(post);
        }


        /**
         * 제목에 AAA가 들어간 POST 생성
         */
        final String SEARCH_TITLE_STR = "AAA";

        final int COND_POST_COUNT = 100;

        for(int i = 1; i<=COND_POST_COUNT; i++ ){
            Post post = Post.builder().title(SEARCH_TITLE_STR+ i).content("내용"+i).build();
            post.confirmWriter(member1);
            postRepository.save(post);
        }
        clear();



        //when
        final int PAGE = 2;
        final int SIZE = 20;
        PageRequest pageRequest = PageRequest.of(PAGE, SIZE);

        PostSearchCondition postSearchCondition = new PostSearchCondition();
        postSearchCondition.setTitle(SEARCH_TITLE_STR);

        PostPagingDto postList = postService.getPostList(pageRequest, postSearchCondition);

        //then
        assertThat(postList.getTotalElementCount()).isEqualTo(COND_POST_COUNT);
        assertThat(postList.getTotalPageCount()).isEqualTo((COND_POST_COUNT % SIZE == 0)
                                                                    ? COND_POST_COUNT/SIZE
                                                                    : COND_POST_COUNT/SIZE + 1);
        assertThat(postList.getCurrentPageNum()).isEqualTo(PAGE);
        assertThat(postList.getCurrentPageElementCount()).isEqualTo(SIZE);
    }


    @Test
    public void 포스트_검색_내용일치() throws Exception {
        //given
        /**
         * MEMBER 저장
         */

        Member member1 = memberRepository.save(Member.builder().username("username1").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이1").role(Role.USER).age(22).build());


        /**
         * 일반 POST 생성
         */
        final int DEFAULT_POST_COUNT = 100;
        for(int i = 1; i<=DEFAULT_POST_COUNT; i++ ){
            Post post = Post.builder().title("게시글"+ i).content("내용"+i).build();
            post.confirmWriter(member1);
            postRepository.save(post);
        }


        /**
         * 제목에 AAA가 들어간 POST 생성
         */
        final String SEARCH_CONTENT_STR = "AAA";

        final int COND_POST_COUNT = 100;

        for(int i = 1; i<=COND_POST_COUNT; i++ ){
            Post post = Post.builder().title("게시글"+ i).content(SEARCH_CONTENT_STR+i).build();
            post.confirmWriter(member1);
            postRepository.save(post);
        }
        clear();



        //when
        final int PAGE = 2;
        final int SIZE = 20;
        PageRequest pageRequest = PageRequest.of(PAGE, SIZE);

        PostSearchCondition postSearchCondition = new PostSearchCondition();
        postSearchCondition.setContent(SEARCH_CONTENT_STR);

        PostPagingDto postList = postService.getPostList(pageRequest, postSearchCondition);

        //then
        assertThat(postList.getTotalElementCount()).isEqualTo(COND_POST_COUNT);
        assertThat(postList.getTotalPageCount()).isEqualTo((COND_POST_COUNT % SIZE == 0)
                ? COND_POST_COUNT/SIZE
                : COND_POST_COUNT/SIZE + 1);
        assertThat(postList.getCurrentPageNum()).isEqualTo(PAGE);
        assertThat(postList.getCurrentPageElementCount()).isEqualTo(SIZE);
    }

    @Test
    public void 포스트_검색_제목과내용일치() throws Exception {
        //given
        /**
         * MEMBER 저장
         */

        Member member1 = memberRepository.save(Member.builder().username("username1").password("1234567890").name("USER1").nickName("밥 잘먹는 동훈이1").role(Role.USER).age(22).build());


        /**
         * 일반 POST 생성
         */
        final int DEFAULT_POST_COUNT = 100;
        for(int i = 1; i<=DEFAULT_POST_COUNT; i++ ){
            Post post = Post.builder().title("게시글"+ i).content("내용"+i).build();
            post.confirmWriter(member1);
            postRepository.save(post);
        }


        /**
         * 제목에 AAA가 들어간 POST 생성
         */
        final String SEARCH_TITLE_STR = "AAA";
        final String SEARCH_CONTENT_STR = "BBB";

        final int COND_POST_COUNT = 100;

        for(int i = 1; i<=COND_POST_COUNT; i++ ){
            Post post = Post.builder().title(SEARCH_TITLE_STR+ i).content(SEARCH_CONTENT_STR+i).build();
            post.confirmWriter(member1);
            postRepository.save(post);
        }
        clear();



        //when
        final int PAGE = 2;
        final int SIZE = 20;
        PageRequest pageRequest = PageRequest.of(PAGE, SIZE);

        PostSearchCondition postSearchCondition = new PostSearchCondition();
        postSearchCondition.setTitle(SEARCH_TITLE_STR);
        postSearchCondition.setContent(SEARCH_CONTENT_STR);

        PostPagingDto postList = postService.getPostList(pageRequest, postSearchCondition);

        //then
        assertThat(postList.getTotalElementCount()).isEqualTo(COND_POST_COUNT);
        assertThat(postList.getTotalPageCount()).isEqualTo((COND_POST_COUNT % SIZE == 0)
                ? COND_POST_COUNT/SIZE
                : COND_POST_COUNT/SIZE + 1);
        assertThat(postList.getCurrentPageNum()).isEqualTo(PAGE);
        assertThat(postList.getCurrentPageElementCount()).isEqualTo(SIZE);
    }

}