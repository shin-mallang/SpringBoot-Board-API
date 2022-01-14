package boardexample.myboard.domain.post.dto;

import boardexample.myboard.domain.post.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public record PostSaveDto(String title, String content, Optional<MultipartFile> uploadFile) {



    public Post toEntity() {
        return Post.builder().title(title).content(content).build();
    }
}
