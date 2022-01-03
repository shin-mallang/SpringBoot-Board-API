package boardexample.myboard.domain.commnet.service;

import boardexample.myboard.domain.commnet.Comment;
import boardexample.myboard.domain.commnet.repository.CommentRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class CommentServiceTest {


    @Autowired
    CommentService commentService;

    @Autowired CommentRepository commentRepository;

    @Autowired EntityManager em;

    private void clear(){
        em.flush();
        em.clear();
    }


    private Long saveComment(){
        Comment comment = Comment.builder().content("댓글").build();
        Long id = commentRepository.save(comment).getId();
        clear();
        return id;
    }

    private Long saveReComment(Long parentId){
        Comment parent = commentRepository.findById(parentId).orElse(null);
        Comment comment = Comment.builder().content("댓글").parent(parent).build();

        Long id = commentRepository.save(comment).getId();
        clear();
        return id;
    }


    // 댓글을 삭제하는 경우
    // 대댓글이 남아있는 경우
    // DB와 화면에서는 지워지지 않고, "삭제된 댓글입니다"라고 표시
    @Test
    public void 댓글삭제_대댓글이_남아있는_경우() throws Exception {
        //given
        Long commentId = saveComment();
        saveReComment(commentId);
        saveReComment(commentId);
        saveReComment(commentId);
        saveReComment(commentId);

        Assertions.assertThat(commentService.findById(commentId).getChildList().size()).isEqualTo(4);

        //when
        commentService.remove(commentId);
        clear();


        //then
        Comment findComment = commentService.findById(commentId);
        assertThat(findComment).isNotNull();
        assertThat(findComment.isRemoved()).isTrue();
        assertThat(findComment.getChildList().size()).isEqualTo(4);
    }




    // 댓글을 삭제하는 경우
    //대댓글이 아예 존재하지 않는 경우 : 곧바로 DB에서 삭제
    @Test
    public void 댓글삭제_대댓글이_없는_경우() throws Exception {
        //given
        Long commentId = saveComment();

        //when
        commentService.remove(commentId);
        clear();

        //then
        Assertions.assertThat(commentService.findAll().size()).isSameAs(0);
        assertThat( assertThrows(Exception.class, () -> commentService.findById(commentId)).getMessage()).isEqualTo("댓글이 없습니다.");
    }




    // 댓글을 삭제하는 경우
    // 대댓글이 존재하나 모두 삭제된 경우
    //댓글과, 달려있는 대댓글 모두 DB에서 일괄 삭제, 화면상에도 표시되지 않음
    @Test
    public void 댓글삭제_대댓글이_존재하나_모두_삭제된_대댓글인_경우() throws Exception {
        //given
        Long commentId = saveComment();
        Long reCommend1Id = saveReComment(commentId);
        Long reCommend2Id = saveReComment(commentId);
        Long reCommend3Id = saveReComment(commentId);
        Long reCommend4Id = saveReComment(commentId);


        Assertions.assertThat(commentService.findById(commentId).getChildList().size()).isEqualTo(4);
        clear();

        commentService.remove(reCommend1Id);
        clear();

        commentService.remove(reCommend2Id);
        clear();

        commentService.remove(reCommend3Id);
        clear();

        commentService.remove(reCommend4Id);
        clear();

        Assertions.assertThat(commentService.findById(reCommend1Id).isRemoved()).isTrue();
        Assertions.assertThat(commentService.findById(reCommend2Id).isRemoved()).isTrue();
        Assertions.assertThat(commentService.findById(reCommend3Id).isRemoved()).isTrue();
        Assertions.assertThat(commentService.findById(reCommend4Id).isRemoved()).isTrue();
        clear();


        //when
        commentService.remove(commentId);
        clear();


        //then
        LongStream.rangeClosed(commentId, reCommend4Id).forEach(id ->
                    assertThat(assertThrows(Exception.class, () -> commentService.findById(commentId)).getMessage()).isEqualTo("댓글이 없습니다.")
        );

    }





    // 대댓글을 삭제하는 경우
    // 부모 댓글이 삭제되지 않은 경우
    // 내용만 삭제, DB에서는 삭제 X
    @Test
    public void 대댓글삭제_부모댓글이_남아있는_경우() throws Exception {
        //given
        Long commentId = saveComment();
        Long reCommend1Id = saveReComment(commentId);


        //when
        commentService.remove(reCommend1Id);
        clear();


        //then
        Assertions.assertThat(commentService.findById(commentId)).isNotNull();
        Assertions.assertThat(commentService.findById(reCommend1Id)).isNotNull();
        Assertions.assertThat(commentService.findById(commentId).isRemoved()).isFalse();
        Assertions.assertThat(commentService.findById(reCommend1Id).isRemoved()).isTrue();
    }



    // 대댓글을 삭제하는 경우
    // 부모 댓글이 삭제되어있고, 대댓글들도 모두 삭제된 경우
    // 부모를 포함한 모든 대댓글을 DB에서 일괄 삭제, 화면상에서도 지움
    @Test
    public void 대댓글삭제_부모댓글이_삭제된_경우_모든_대댓글이_삭제된_경우() throws Exception {
        //given
        Long commentId = saveComment();
        Long reCommend1Id = saveReComment(commentId);
        Long reCommend2Id = saveReComment(commentId);
        Long reCommend3Id = saveReComment(commentId);


        commentService.remove(reCommend2Id);
        clear();
        commentService.remove(commentId);
        clear();
        commentService.remove(reCommend3Id);
        clear();


        Assertions.assertThat(commentService.findById(commentId)).isNotNull();
        Assertions.assertThat(commentService.findById(commentId).getChildList().size()).isEqualTo(3);

        //when
        commentService.remove(reCommend1Id);



        //then
        LongStream.rangeClosed(commentId, reCommend3Id).forEach(id ->
                assertThat(assertThrows(Exception.class, () -> commentService.findById(commentId)).getMessage()).isEqualTo("댓글이 없습니다.")
        );



    }


    // 대댓글을 삭제하는 경우
    // 부모 댓글이 삭제되어있고, 다른 대댓글이 아직 삭제되지 않고 남아있는 경우
    //해당 대댓글만 삭제, 그러나 DB에서 삭제되지는 않고, 화면상에는 "삭제된 댓글입니다"라고 표시
    @Test
    public void 대댓글삭제_부모댓글이_삭제된_경우_다른_대댓글이_남아있는_경우() throws Exception {
        //given
        Long commentId = saveComment();
        Long reCommend1Id = saveReComment(commentId);
        Long reCommend2Id = saveReComment(commentId);
        Long reCommend3Id = saveReComment(commentId);


        commentService.remove(reCommend3Id);
        commentService.remove(commentId);
        clear();

        Assertions.assertThat(commentService.findById(commentId)).isNotNull();
        Assertions.assertThat(commentService.findById(commentId).getChildList().size()).isEqualTo(3);


        //when
        commentService.remove(reCommend2Id);
        Assertions.assertThat(commentService.findById(commentId)).isNotNull();


        //then
        Assertions.assertThat(commentService.findById(reCommend2Id)).isNotNull();
        Assertions.assertThat(commentService.findById(reCommend2Id).isRemoved()).isTrue();
        Assertions.assertThat(commentService.findById(reCommend1Id).getId()).isNotNull();
        Assertions.assertThat(commentService.findById(reCommend3Id).getId()).isNotNull();
        Assertions.assertThat(commentService.findById(commentId).getId()).isNotNull();

    }

}