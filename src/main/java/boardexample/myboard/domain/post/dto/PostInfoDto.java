package boardexample.myboard.domain.post.dto;

import boardexample.myboard.domain.commnet.Comment;
import boardexample.myboard.domain.commnet.dto.CommentInfoDto;
import boardexample.myboard.domain.commnet.dto.ReCommentInfoDto;
import boardexample.myboard.domain.member.dto.MemberInfoDto;
import boardexample.myboard.domain.post.Post;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class PostInfoDto{


    private Long postId; //POST의 ID
    private String title;//제목
    private String content;//내용
    private String filePath;//업로드 파일 경로

    private MemberInfoDto writerDto;//작성자에 대한 정보


    private List<CommentInfoDto> commentInfoDtoList;//댓글 정보들




    public PostInfoDto(Post post) {

        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.filePath = post.getFilePath();


        this.writerDto = new MemberInfoDto(post.getWriter());




        /**
         * 댓글과 대댓글을 그룹짓기
         * post.getCommentList()는 댓글과 대댓글이 모두 조회된다.
         */

        Map<Comment, List<Comment>> commentListMap = post.getCommentList().stream()

                                                     .filter(comment -> comment.getParent() != null)

                                                     .collect(Collectors.groupingBy(Comment::getParent));






        /**
         * 댓글과 대댓글을 통해 CommentInfoDto 생성
         */

        commentInfoDtoList = commentListMap.keySet().stream()

                                            .map(comment -> new CommentInfoDto(comment, commentListMap.get(comment)))
                                            .toList();

    }
}
