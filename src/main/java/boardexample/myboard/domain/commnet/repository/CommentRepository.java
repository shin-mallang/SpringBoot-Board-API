package boardexample.myboard.domain.commnet.repository;

import boardexample.myboard.domain.commnet.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
