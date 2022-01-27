package boardexample.myboard.domain.commnet.service;


import boardexample.myboard.domain.commnet.Comment;
import boardexample.myboard.domain.commnet.dto.CommentSaveDto;
import boardexample.myboard.domain.commnet.dto.CommentUpdateDto;
import boardexample.myboard.domain.commnet.exception.CommentException;

import java.util.List;

public interface CommentService {

    void save(Long postId , CommentSaveDto commentSaveDto);
    void saveReComment(Long postId, Long parentId ,CommentSaveDto commentSaveDto);

    void update(Long id, CommentUpdateDto commentUpdateDto);

    void remove(Long id) throws CommentException;
}
