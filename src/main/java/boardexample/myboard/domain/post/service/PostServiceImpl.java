package boardexample.myboard.domain.post.service;

import boardexample.myboard.domain.member.exception.MemberException;
import boardexample.myboard.domain.member.exception.MemberExceptionType;
import boardexample.myboard.domain.member.repository.MemberRepository;
import boardexample.myboard.domain.post.Post;
import boardexample.myboard.domain.post.cond.PostSearchCondition;
import boardexample.myboard.domain.post.dto.PostInfoDto;
import boardexample.myboard.domain.post.dto.PostPagingDto;
import boardexample.myboard.domain.post.dto.PostSaveDto;
import boardexample.myboard.domain.post.dto.PostUpdateDto;
import boardexample.myboard.domain.post.exception.PostException;
import boardexample.myboard.domain.post.exception.PostExceptionType;
import boardexample.myboard.domain.post.repository.PostRepository;
import boardexample.myboard.global.file.exception.FileException;
import boardexample.myboard.global.file.service.FileService;
import boardexample.myboard.global.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;


    @Override
    public void save(PostSaveDto postSaveDto) throws FileException {
        Post post = postSaveDto.toEntity();

        post.confirmWriter(memberRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER)));

        postSaveDto.uploadFile().ifPresent(
                file ->  post.updateFilePath(fileService.save(file))
        );
        postRepository.save(post);
    }

    @Override
    public void update(Long id, PostUpdateDto postUpdateDto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostException(PostExceptionType.POST_NOT_POUND));
        checkAuthority(post,PostExceptionType.NOT_AUTHORITY_UPDATE_POST );


        postUpdateDto.title().ifPresent(post::updateTitle);
        postUpdateDto.content().ifPresent(post::updateContent);


        if(post.getFilePath() !=null){
            fileService.delete(post.getFilePath());//기존에 올린 파일 지우기
        }

        postUpdateDto.uploadFile().ifPresentOrElse(
                multipartFile ->  post.updateFilePath(fileService.save(multipartFile)),
                () ->  post.updateFilePath(null)
                );

    }



    @Override
    public void delete(Long id) {

        Post post = postRepository.findById(id).orElseThrow(() ->
                new PostException(PostExceptionType.POST_NOT_POUND));

        checkAuthority(post,PostExceptionType.NOT_AUTHORITY_DELETE_POST);


        if(post.getFilePath() !=null){
            fileService.delete(post.getFilePath());//기존에 올린 파일 지우기
        }

        postRepository.delete(post);
    }


    private void checkAuthority(Post post, PostExceptionType postExceptionType) {
        if(!post.getWriter().getUsername().equals(SecurityUtil.getLoginUsername()))
            throw new PostException(postExceptionType);
    }

    @Override
    public PostInfoDto getPostInfo(Long id) {
        return null;
    }

    @Override
    public PostPagingDto getPostList(int page, int pageSize, PostSearchCondition postSearchCondition) {
        return null;
    }
}
