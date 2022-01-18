package boardexample.myboard.domain.commnet.dto;

import boardexample.myboard.domain.commnet.Comment;

import java.util.Optional;

public record CommentUpdateDto (Optional<String> content){ }
