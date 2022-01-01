package boardexample.myboard.domain.commnet;


import boardexample.myboard.domain.BaseTimeEntity;
import boardexample.myboard.domain.member.Member;
import boardexample.myboard.domain.post.Post;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Table(name="COMMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Comment extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "comment_id")
    private Long id;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "writer_id")
    private Member writer;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;



    //== 부모 댓글을 삭제해도 자식 댓글은 남아있음 ==//
    @OneToMany(mappedBy = "parent")
    private List<Comment> childList = new ArrayList<>();



    @Lob
    @Column(nullable = false)
    private String content;




    //== 연관관계 편의 메서드 ==//
    public void confirmWriter(Member writer) {
        this.writer = writer;
        writer.addComment(this);
    }

    public void confirmPost(Post post) {
        this.post = post;
        post.addComment(this);
    }

    public void confirmParent(Comment parent){
        this.parent = parent;
        parent.addChild(this);
    }

    public void addChild(Comment child){
        childList.add(child);
    }


    //== 수정 ==//
    public void updateContent(String content) {
        this.content = content;
    }


    /**
     * 댓글을 삭제하는 경우 - 2가지
     *
     *   1. 댓글을 삭제하는 경우
     *   2. 대댓글을 삭제하는 경우
     *
     *      1 - 1. 댓글을 삭제하는 경우 - 대댓글이 남아있을 때
     *          내용은 지워지나, DB에서 사라지지는 않음,
     *
     *      1 - 2. 댓글을 삭제하는 경우 - 대댓글이 없을 때
     *          곧바로 삭제 (지워진 대댓글들도 DB에서 모두 지워주어야 함)
     *
     *
     *      2 - 1. 대댓글을 삭제하는 경우 - 부모가 삭제되지 않은 댓글일 경우
     *          2 - 1 - 1. 대댓글만 삭제(내용을 비움, 부모가 삭제되기 전까지 DB에 존재시킴)
     *
     *      2 - 2. 대댓글을 삭제하는 경우 - 부모가 삭제된 댓글인 경우
     *              2 - 2 - 1. 이번에 삭제한 대댓글로 인해, 부모의 모든 대댓글이 삭제되는 경우 -> 부모의 댓글과, 달려있는 대댓글 모두 삭제(DB에서 삭제),
     *              2 - 2 - 2. 아직 다른 대댓글이 남아있는 경우 -> 현재 대댓글의 내용만 지워줌
     *
     */

}
