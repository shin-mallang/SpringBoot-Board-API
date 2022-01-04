package boardexample.myboard.domain.post.repository;

import boardexample.myboard.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
