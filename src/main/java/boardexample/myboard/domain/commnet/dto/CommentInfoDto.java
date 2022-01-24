package boardexample.myboard.domain.commnet.dto;

import boardexample.myboard.domain.commnet.Comment;
import boardexample.myboard.domain.member.dto.MemberInfoDto;
import lombok.Data;

import java.util.List;

@Data
public class CommentInfoDto {

    private final static String DEFAULT_DELETE_MESSAGE = "삭제된 댓글입니다";

    private Long postId;//댓글이 달린 POST의 ID


    private Long commentId;//해당 댓글의 ID
    private String content;//내용 (삭제되었다면 "삭제된 댓글입니다 출력")
    private boolean isRemoved;//삭제되었는지?


    private MemberInfoDto writerDto;//댓글 작성자에 대한 정보

    private List<ReCommentInfoDto> reCommentListDtoList;//대댓글에 대한 정보들





    /**
     * 삭제되었을 경우 삭제된 댓글입니다 출력
     */

    public CommentInfoDto(Comment comment, List<Comment> reCommentList) {

        this.postId = comment.getPost().getId();
        this.commentId = comment.getId();


        this.content = comment.getContent();

        if(comment.isRemoved()){
            this.content = DEFAULT_DELETE_MESSAGE;
        }

        this.isRemoved = comment.isRemoved();



        this.writerDto = new MemberInfoDto(comment.getWriter());

        this.reCommentListDtoList = reCommentList.stream().map(ReCommentInfoDto::new).toList();

    }
}
