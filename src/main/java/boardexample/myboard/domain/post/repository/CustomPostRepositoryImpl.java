package boardexample.myboard.domain.post.repository;

import boardexample.myboard.domain.post.Post;
import boardexample.myboard.domain.post.cond.PostSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static boardexample.myboard.domain.member.QMember.member;
import static boardexample.myboard.domain.post.QPost.post;

@Repository
public class CustomPostRepositoryImpl implements CustomPostRepository {

    private final JPAQueryFactory query;

    public CustomPostRepositoryImpl(EntityManager em) {
        query = new JPAQueryFactory(em);
    }


    @Override
    public Page<Post> search(PostSearchCondition postSearchCondition, Pageable pageable) {



        List<Post> content = query.selectFrom(post)

                                    .where(
                                            contentHasStr(postSearchCondition.getContent()),
                                            titleHasStr(postSearchCondition.getTitle())
                                        )
                .leftJoin(post.writer, member)

                .fetchJoin()
                                    .orderBy(post.createdDate.desc())//최신 날짜부터
                                    .offset(pageable.getOffset())
                                    .limit(pageable.getPageSize())
                                    .fetch(); //Count 쿼리 발생 X




        JPAQuery<Post> countQuery = query.selectFrom(post)
                                            .where(
                                                    contentHasStr(postSearchCondition.getContent()),
                                                    titleHasStr(postSearchCondition.getTitle())
                                            );



        return  PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
    }

    private BooleanExpression contentHasStr(String content) {
        return StringUtils.hasLength(content) ? post.content.contains(content) : null;
    }


    private BooleanExpression titleHasStr(String title) {
        return StringUtils.hasLength(title) ? post.title.contains(title) : null;
    }
}
