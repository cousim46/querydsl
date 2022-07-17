package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDTO;
import study.querydsl.dto.QMemberTeamDTO;
import study.querydsl.entity.Member;

import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"), member.username,
                        member.age,
                        team.id.as("teamId")
                        , team.name.as("teamName")
                )).from(member)
                .leftJoin(member.team, team)
                .where(ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()),
                        teamNameEq(condition.getTeamName()),
                        userNameEq(condition.getUsername()))
                .fetch();
    }

    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDTO> results = queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"), member.username,
                        member.age,
                        team.id.as("teamId")
                        , team.name.as("teamName")
                )).from(member)
                .leftJoin(member.team, team)
                .where(ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()),
                        teamNameEq(condition.getTeamName()),
                        userNameEq(condition.getUsername()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        List<MemberTeamDTO> content = results.getResults();
        for (MemberTeamDTO memberTeamDTO : content) {
            System.out.println("memberTeamDTO = " + memberTeamDTO);
        }
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> content = queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"), member.username,
                        member.age,
                        team.id.as("teamId")
                        , team.name.as("teamName")
                )).from(member)
                .leftJoin(member.team, team)
                .where(ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()),
                        teamNameEq(condition.getTeamName()),
                        userNameEq(condition.getUsername()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Member> countQuery = queryFactory.select(member)
                .from(member).leftJoin(member.team, team)
                .where(ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()),
                        teamNameEq(condition.getTeamName()),
                        userNameEq(condition.getUsername()));
        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
        // return new PageImpl<>(content, pageable, total);
    }


    private BooleanExpression ageBetween(int ageLoe, int ageGoe) {
        return ageGoe(ageGoe).and(ageLoe(ageLoe));
    }

    public BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    public BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.goe(ageLoe) : null;
    }

    public BooleanExpression teamNameEq(String teamName) {
        return teamName != null ? team.name.eq(teamName) : null;
    }

    public BooleanExpression userNameEq(String username) {
        return username != null ? member.username.eq(username) : null;
    }

}
