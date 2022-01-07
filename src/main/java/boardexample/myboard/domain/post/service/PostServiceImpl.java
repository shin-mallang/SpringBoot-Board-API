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
                .orElseThrow(() -> new PostException(PostExceptionType.POST_NOT_POUND)));

        postRepository.save(post);
    }

    @Override
    public void update(Long id, PostUpdateDto postUpdateDto) {

        Post post = postRepository.findById(id).orElseThrow(() ->
                new PostException(PostExceptionType.POST_NOT_POUND));

        if(!post.getWriter().getUsername().equals(SecurityUtil.getLoginUsername())){
            throw new PostException(PostExceptionType.NOT_AUTHORITY_UPDATE_POST);
        }

        postUpdateDto.title().ifPresent(title -> post.updateTitle(title));
        postUpdateDto.content().ifPresent(content -> post.updateContent(content));
        // TODO : 파일 저장 해야함   postUpdateDto.uploadFile().ifPresent();

    }

    @Override
    public void delete(Long id) {

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
