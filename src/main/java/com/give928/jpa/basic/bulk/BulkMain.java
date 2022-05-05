package com.give928.jpa.basic.bulk;

import com.give928.jpa.basic.domain.Member;
import com.give928.jpa.basic.domain.RoleType;
import com.give928.jpa.basic.domain.embeddable.Address;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class BulkMain {
    static EntityManagerFactory emf;
    static EntityManager em;
    static EntityTransaction tx;

    static {
        emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        em = emf.createEntityManager();
        tx = em.getTransaction();
    }

    public static void main(String[] args) {
        tx.begin();
        try {
            initializeMembers(100);

            em.flush();
            em.clear();

            bulk();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void bulk() {
        int updateCount = em.createQuery("update Member as m set m.age = m.age + 1")
                .executeUpdate();

        System.out.println("updateCount = " + updateCount);

        // 영속성 컨텍스트를 무시하고 데이터베이스에 직접 실행한다.
        // 따라서 벌크 연산 수행 후에는 영속성 컨텍스트를 초기화 한다.
        em.clear();
    }

    private static void initializeMembers(int count) throws NoSuchAlgorithmException {
        Random r = SecureRandom.getInstanceStrong();
        for (int i = 0; i < count; i++) {
            int c = i + 1;
            String no = "";
            if (c < 10) {
                no += "00";
            } else if (c < 100) {
                no += "0";
            }
            no += c;

            int age = r.nextInt(100);

            Member member = Member.builder()
                    .username("kim" + no)
                    .age(age)
                    .roleType(age % 2 == 0 ? RoleType.ADMIN : RoleType.USER)
                    .homeAddress(Address.builder().city("city" + no).street("street" + no).zipcode("11" + no).build())
                    .build();
            em.persist(member);
        }
    }
}
