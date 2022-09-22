package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;


@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        //save
        Member member = new Member("memberV0", 10000);
        repository.save(member); // 여기서 SQLException 올라옴.

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(member); //내부적으로 equals
        assertThat(findMember).isSameAs(member); // == 비교 이므로 false
        //findMember와 member 의 == 비교는 false 지만
        //equals 비교는 true이다.

        //update: money를 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        //delete
        repository.delete(member.getMemberId());

//        Member findMember2 = repository.findById(member.getMemberId());
        // 없는 데이터 조회 시 NoSuchElementException 터지게 해둠
        // 그러므로 NoSuch ElementException 이 터지면 Testcase 성공하게 만들자.
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
    }
}
