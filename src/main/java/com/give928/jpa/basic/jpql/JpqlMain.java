package com.give928.jpa.basic.jpql;

import com.give928.jpa.basic.domain.Member;
import com.give928.jpa.basic.domain.Team;
import com.give928.jpa.basic.domain.embeddable.Address;
import com.give928.jpa.basic.dto.MemberDTO;

import javax.persistence.*;
import java.util.List;

public class JpqlMain {
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
//            jpql();
            projection();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void projection() {
        Team team = Team.builder().name("teamA").build();
        em.persist(team);

        Member member1 = Member.builder()
                .username("kim1")
                .age(20)
                .homeAddress(Address.builder().city("city1").street("street1").zipcode("11111").build())
                .team(team)
                .build();
        em.persist(member1);

        Member member2 = Member.builder()
                .username("kim2")
                .age(20)
                .homeAddress(Address.builder().city("city2").street("street2").zipcode("11112").build())
                .team(team)
                .build();
        em.persist(member2);

        // 엔티티 프로젝션
        List<Team> teams = em.createQuery("select t from Member as m inner join m.team as t", Team.class)
                .getResultList();
        System.out.println("teams = " + teams);
        for (Team team1 : teams) {
            System.out.println("team1.getMembers() = " + team1.getMembers());
        }

        // 임베디드 타입 프로젝션
        List<Address> addresses = em.createQuery("select m.homeAddress from Member as m", Address.class)
                .getResultList();
        for (Address address : addresses) {
            System.out.println("address = " + address);
        }

        // 스칼라 타입 프로젝션
        List<Object[]> memberObjects = em.createQuery("select distinct m.username, m.age from Member as m", Object[].class)
                .getResultList();
        for (Object[] memberObject : memberObjects) {
            System.out.printf("memberObject[0]=%s, memberObject[1]=%s%n", memberObject[0], memberObject[1]);
        }

        List<MemberDTO> memberDTOs = em.createQuery("select distinct new com.give928.jpa.basic.dto.MemberDTO(m.username, m.age) from Member as m", MemberDTO.class)
                .getResultList();
        for (MemberDTO memberDTO : memberDTOs) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    private static void jpql() {
        Member member = Member.builder()
                .username("kim1")
                .build();
        em.persist(member);

//            TypedQuery<String> usernameQuery = em.createQuery(
//                    "select m.username from Member as m where m.username like '%kim%'", String.class);
//
//            Query usernameAndAgeQuery = em.createQuery(
//                    "select m.username, m.age from Member as m where m.username like '%kim%'");

        TypedQuery<Member> membersQuery = em.createQuery(
                "select m from Member as m where m.username like :username",
                Member.class);
        membersQuery.setParameter("username", "%kim%");
        List<Member> findMembers = membersQuery.getResultList();
        for (Member m : findMembers) {
            System.out.println("m = " + m);
        }

        // method chaining
        Member findMember = em.createQuery("select m from Member as m where m.id = :id",
                                           Member.class)
                .setParameter("id", member.getId())
                .getSingleResult();
        System.out.println("findMember = " + findMember);

        tx.commit();
    }
}
