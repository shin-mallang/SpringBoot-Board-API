package boardexample.myboard.domain.commnet.service;

import boardexample.myboard.domain.commnet.Comment;
import boardexample.myboard.domain.commnet.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;


    @Override
    public void save(Comment comment) {

        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Comment findById(Long id) throws Exception {
        return commentRepository.findById(id).orElseThrow(() -> new Exception("댓글이 없습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }



    @Override
    public void remove(Long id) throws Exception {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new Exception("댓글이 없습니다."));
        comment.removeContent();
        List<Comment> removableCommentList = comment.findRemovableList();
        removableCommentList.forEach(removableComment -> commentRepository.delete(removableComment));
    }
}
