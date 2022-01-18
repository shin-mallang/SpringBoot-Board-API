package boardexample.myboard.domain.commnet.dto;

import boardexample.myboard.domain.commnet.Comment;
import boardexample.myboard.domain.post.Post;

public record CommentSaveDto (String content){

    public Comment toEntity() {
        return Comment.builder().content(content).build();
    }
}
