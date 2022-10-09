package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 * DataSourceUtils.getConnection() // 커넥션 가져옴
 * DataSourceUtils.releaseConnection() // 커넥션 반납
 */
@Slf4j
@RequiredArgsConstructor
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;//이걸로 쿼리 날림

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql); // 여기서 CheckedException인 SQLException이 올라온다.
            pstmt.setString(1, member.getMemberId());//sql에 대한 파라미터 바인딩
            pstmt.setInt(2, member.getMoney());//sql에 대한 파라미터 바인딩 (?, ?) 에 바인딩
            pstmt.executeUpdate();//쿼리가 DB에 실제 실행 (업데이트에 영향받은 row 수 반환)
            //데이터 변경 쿼리 시에는 executeUpdate()
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e; // throw new RuntimeException(e)하면 위 클래스로 throws 안해도 되지만
            // throw e; 하면 위 클래스로 throws 한다. (이때 class에 throws SQLException 해줘야함.)
        } finally { // 시작과 역순으로 close
            //외부 리소스를 쓰는 중이다. (실제 TCP/IP 커넥션걸려있음)
            //안 닫아주면 계속 유지됨.-> 리소스 낭비
//            pstmt.close(); // 만약 여기서 Exception 터지면 이게 위 클래스로 나가고 con.close(); 가 호출 안됨.
//            con.close();
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id=?";//항상 파라미터바인딩해주자.
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();//select는 executeQuery로 실행
            //이때 결과를 담고있는 통인 ResultSet을 반환해줌.

            //rs에서 값을 꺼내자
            //rs내부에 커서가 있는데 이 커서를 한번 next해줘야 실제 값이 있다.
            //커서가 처음에는 아무것도 안가르킴.(next로 넘겼을 때 데이터가 있으면 true)
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else { //next하고 커서가 가리키는 데이터가 없을 경우
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);//해제
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        //사용한 자원들 다 닫아주자.

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야한다.
        DataSourceUtils.releaseConnection(con, dataSource);
//        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {

        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }

}
