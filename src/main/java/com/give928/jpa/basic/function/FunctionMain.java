package com.give928.jpa.basic.function;

import com.give928.jpa.basic.domain.Member;
import com.give928.jpa.basic.domain.Order;
import com.give928.jpa.basic.domain.OrderStatus;
import com.give928.jpa.basic.domain.RoleType;
import com.give928.jpa.basic.domain.embeddable.Address;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class FunctionMain {
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
            initializeMembers(10);

            em.flush();
            em.clear();

            customFunction();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void customFunction() {
        // dialect를 상속받아서 생성자에서 registerFunction 을 하고 persistence.xml 에 hibernate.dialect 를 변경
//        String qlString = "select group_concatconcat(m.username) from Member as m";
        String qlString = "select function('group_concat', m.username) from Member as m";
        List<String> resultList = em.createQuery(qlString, String.class).getResultList();

        for (String s : resultList) {
            System.out.println("s = " + s);
        }
    }

    private static void initializeMembers(int count) throws NoSuchAlgorithmException {
        Random random = SecureRandom.getInstanceStrong();
        for (int i = 0; i < count; i++) {
            int c = i + 1;
            String no = "";
            if (c < 10) {
                no += "00";
            } else if (c < 100) {
                no += "0";
            }
            no += c;

            int age = random.nextInt(10);

            Member member = Member.builder()
                    .username("kim" + no)
                    .age(age)
                    .roleType(age % 2 == 0 ? RoleType.ADMIN : RoleType.USER)
                    .homeAddress(Address.builder().city("city" + no).street("street" + no).zipcode("11" + no).build())
                    .build();
            em.persist(member);

            if (age % 7 == 0) {
                Order order = Order.builder().orderDate(LocalDateTime.now()).status(OrderStatus.ORDER).member(
                        member).build();
                em.persist(order);
            }
        }
    }
}
