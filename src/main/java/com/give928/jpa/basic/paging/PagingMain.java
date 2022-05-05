package com.give928.jpa.basic.paging;

import com.give928.jpa.basic.domain.Member;
import com.give928.jpa.basic.domain.Team;
import com.give928.jpa.basic.domain.embeddable.Address;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PagingMain {
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

            paging();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void paging() {
        String qlString = "select m from Member as m order by m.age desc";
        List<Member> members = em.createQuery(qlString, Member.class)
                .setFirstResult(20)
                .setMaxResults(10)
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Member member : members) {
            System.out.println("member = " + member);
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

            Team team = teams.get(r.nextInt(10));

            Member member1 = Member.builder()
                    .username("kim" + no)
                    .age(c)
                    .homeAddress(Address.builder().city("city" + no).street("street" + no).zipcode("11" + no).build())
                    .team(team)
                    .build();
            em.persist(member1);
        }
    }
}
