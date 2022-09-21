package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * JDBC - DriverManger 사용 ( 가장 Low 레벨 )
 */
@Slf4j
public class MemberRepositoryV0 {
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;//이걸로 쿼리 날림

        try {
            con = getConnection();
            con.prepareStatement(sql); // 여기서 CheckedException인 SQLException이 올라온다.
            pstmt.setString(1, member.getMemberId());//sql에 대한 파라미터 바인딩
            pstmt.setInt(2, member.getMoney());//sql에 대한 파라미터 바인딩 (?, ?) 에 바인딩
            pstmt.executeUpdate();//쿼리가 DB에 실제 실행 (업데이트에 영향받은 row 수 반환)
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

    private void close(Connection con, Statement stmt, ResultSet rs) {
        //사용한 자원들 다 닫아주자.
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("error", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("error", e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("error", e);
            }

        }
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }

}
