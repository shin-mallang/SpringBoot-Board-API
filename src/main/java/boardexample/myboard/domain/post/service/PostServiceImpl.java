package boardexample.myboard.domain.post.service;

import boardexample.myboard.domain.member.Member;
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

    @Override
    public void save(PostSaveDto postSaveDto) {
        Post post = postSaveDto.toEntity();

        post.confirmWriter(memberRepository.findByUsername(SecurityUtil.getLoginUsername())
                .orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER)));

        /*postSaveDto.multipartFile().ifPresent( multipartFile -> {
            String uploadFilePath = fileService.uploadFile(multipartFile);
            post.updateFilePath(uploadFilePath);

        });*/
        // TODO : 파일 저장 해야함   postUpdateDto.uploadFile().ifPresent();

        postRepository.save(post);
    }

    @Override
    public void update(Long id, PostUpdateDto postUpdateDto) {

        Post post = postRepository.findById(id).orElseThrow(() ->
                new PostException(PostExceptionType.POST_NOT_POUND));

        checkAuthority(post,PostExceptionType.NOT_AUTHORITY_UPDATE_POST );

        postUpdateDto.title().ifPresent(post::updateTitle);
        postUpdateDto.content().ifPresent(post::updateContent);

        // TODO : 파일 저장 해야함   postUpdateDto.uploadFile().ifPresent();

    }

    @Override
    public void delete(Long id) {
        //TODO : 고민, 이미 없는 포스트를 삭제 요청을 보냈는데 굳이..?

        Post post = postRepository.findById(id).orElseThrow(() ->
                new PostException(PostExceptionType.POST_NOT_POUND));

        checkAuthority(post,PostExceptionType.NOT_AUTHORITY_DELETE_POST);

        // TODO : 올려진 파일이 있느면 파일 삭제 해야함   post.deleteFile();?? or FileService.deleteAll??
        postRepository.delete(post);


    }

    private void checkAuthority(Post post, PostExceptionType postExceptionType) {
        if(!post.getWriter().getUsername().equals(SecurityUtil.getLoginUsername()))
            throw new PostException(postExceptionType);
    }

    @Override
    public PostInfoDto get(Long id) {
        return null;
    }

    @Override
    public PostPagingDto getList(int offset, int pageSize, PostSearchCondition postSearchCondition) {
        return null;
    }
}
