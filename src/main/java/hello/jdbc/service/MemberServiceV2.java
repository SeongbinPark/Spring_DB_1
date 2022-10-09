package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

    private final MemberRepositoryV2 memberRepository;
    private final DataSource dataSource; // dataSoure 추가해야한다.

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false);//트랜잭션 시작
            bizLogic(fromId, toId, money, con);
            con.commit(); // 성공시 커밋
        } catch (Exception e) {
            con.rollback(); // 실패시 롤백
            throw new IllegalStateException(e); // 기존 예외 감싸서 던짐
        } finally {
            if (con != null) {
                try {
                    //con.close(); 하면 풀로 돌아가는데 오토커밋이 false인 상태로 돌아간다.
                    //-> true로 바꿔준 후 풀에 돌려주자.
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception e) {
                    log.info("error", e);//exception을 로그로 남길 때는 {}를 안씀, 그냥 , e 넣으면됨.
                }
            }
        }
    }

    private void bizLogic(String fromId, String toId, int money, Connection con) throws SQLException {
        //비지니스로직
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);//보내는사람 돈 감소
        validation(toMember);//문제 생기면 두번째 못 넘어가는 상황 연출
        memberRepository.update(con, toId, toMember.getMoney() + money);//받는사람 돈 증가
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
