package boardexample.myboard.domain.post.service;

import boardexample.myboard.domain.post.cond.PostSearchCondition;
import boardexample.myboard.domain.post.dto.PostInfoDto;
import boardexample.myboard.domain.post.dto.PostPagingDto;
import boardexample.myboard.domain.post.dto.PostSaveDto;
import boardexample.myboard.domain.post.dto.PostUpdateDto;
import boardexample.myboard.global.file.exception.FileException;

import java.io.IOException;

public interface PostService {
    /**
     * 게시글 등록
     * 게시글 수정
     * 게시글 삭제
     * 게시글 조회
     * 페이징
     */

    void save(PostSaveDto postSaveDto) throws FileException;

    void update(Long id, PostUpdateDto postUpdateDto);

    void delete(Long id);

    PostInfoDto getPostInfo(Long id);

    PostPagingDto getPostList(int offset, int pageSize, PostSearchCondition postSearchCondition);
}
