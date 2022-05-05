package com.give928.jpa.basic.join;

import com.give928.jpa.basic.domain.*;
import com.give928.jpa.basic.domain.embeddable.Address;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JoinMain {
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

//            join();
//            joinOn1();
//            joinOn2();

//            subQuery1();
//            subQuery2();

            type();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void join() {
        String qlString = "select m from Member as m inner join m.team t where t.name = :teamName";
        List<Member> members = em.createQuery(qlString, Member.class)
                .setParameter("teamName", "team1")
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    private static void joinOn1() {
        String qlString = "select m from Member as m inner join m.team as t on t.id = :teamId";
        List<Member> members = em.createQuery(qlString, Member.class)
                .setParameter("teamId", 1L)
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    private static void joinOn2() {
        String qlString = "select m from Member as m inner join Team as t on m.team = t and t.id = :teamId";
        List<Member> members = em.createQuery(qlString, Member.class)
                .setParameter("teamId", 1L)
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    private static void subQuery1() {
        String qlString = "select m from Member as m where m.age > (select avg(m2.age) + 20 from Member as m2)";
        List<Member> members = em.createQuery(qlString, Member.class)
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    private static void subQuery2() {
        // 주문이 1건 이상인 회원
//        String qlString = "select m from Member as m where (select count(o) from Order o where m = o.member) > 0";
        // team1 소속인 회원
//        String qlString = "select m from Member as m where exists (select t from m.team t where t.name = 'team1')";
        // 전체 상품 각각의 재고보다 주문량이 많은 주문들
        // select o from Order o where o.orderAmount > ALL (select p.stockAmount from Product p)
        // 어떤 팀이든 팀에 소속된 회원
        String qlString = "select m from Member as m where m.team = ANY (select t from Team t)";

        List<Member> members = em.createQuery(qlString, Member.class)
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    private static void type() {
        String qlString = "select m.username, 'hello', TRUE, 10L from Member as m where m.roleType = :roleType";

        List<Object[]> members = em.createQuery(qlString, Object[].class)
                .setParameter("roleType", RoleType.ADMIN)
                .setFirstResult(0)
                .setMaxResults(20)
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Object[] member : members) {
            System.out.printf("m.username=%s string=%s boolean=%s Long=%s%n", member[0], member[1], member[2], member[3]);
        }
    }

    private static void initializeMembers(int count) throws NoSuchAlgorithmException {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < count / 10; i++) {
            Team team = Team.builder().name("team" + (i + 1)).build();
            teams.add(team);
            em.persist(team);
        }

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

            Team team = teams.get(age / 10);

            Member member = Member.builder()
                    .username("kim" + no)
                    .age(age)
                    .roleType(age % 2 == 0 ? RoleType.ADMIN : RoleType.USER)
                    .homeAddress(Address.builder().city("city" + no).street("street" + no).zipcode("11" + no).build())
                    .team(age < 30 ? team : null)
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
