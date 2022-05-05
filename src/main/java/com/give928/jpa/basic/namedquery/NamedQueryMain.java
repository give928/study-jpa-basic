package com.give928.jpa.basic.namedquery;

import com.give928.jpa.basic.domain.*;
import com.give928.jpa.basic.domain.embeddable.Address;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NamedQueryMain {
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

            namedQuery();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void namedQuery() {
        List<Member> members = em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", "kim001")
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    private static void initializeMembers(int count) {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < count / 10; i++) {
            Team team = Team.builder().name("team" + (i + 1)).build();
            teams.add(team);
            em.persist(team);
        }

        Random r = new Random();
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

            int hasTeam = r.nextInt(4);

            Member member = Member.builder()
                    .username("kim" + no)
                    .age(age)
                    .roleType(age % 2 == 0 ? RoleType.ADMIN : RoleType.USER)
                    .homeAddress(Address.builder().city("city" + no).street("street" + no).zipcode("11" + no).build())
                    .team(hasTeam == 0 ? null : teams.get(r.nextInt(10)))
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
