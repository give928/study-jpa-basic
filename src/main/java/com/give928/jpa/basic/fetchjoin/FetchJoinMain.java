package com.give928.jpa.basic.fetchjoin;

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

public class FetchJoinMain {
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

//            entityFetchJoin();
//            collectionFetchJoin();
//            distinctCollectionFetchJoin();
//            dontUseAliasAndFilteringCollectionFetchJoin();
            collectionFetchJoinPagingAlternative();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void entityFetchJoin() {
        String qlString = "select m from Member as m inner join fetch m.team as t where t.name = :teamName";
        List<Member> members = em.createQuery(qlString, Member.class)
                .setParameter("teamName", "team1")
                .getResultList();

        System.out.println("members.size() = " + members.size());
        for (Member member : members) {
            System.out.println("member = " + member + ", team = " + member.getTeam().getName());
        }
    }

    private static void collectionFetchJoin() {
        String qlString = "select t from Team as t inner join fetch t.members where t.name = :teamName";
        List<Team> teams = em.createQuery(qlString, Team.class)
                .setParameter("teamName", "team1")
                .getResultList();

        // 동일한 데이터가 Team과 Member의 수의 곱 만큼 조회된다.
        // 팀1에 멤버가 10명이면 1개의 팀 엔티티에 멤버 컬렉션에 10개가 담겨서 10건이 조회된다.
        System.out.println("teams.size() = " + teams.size());
        for (Team team : teams) {
            System.out.println("team = " + team);
            for (Member member : team.getMembers()) {
                System.out.println("member = " + member);
            }
        }
    }

    private static void distinctCollectionFetchJoin() {
        // SQL 에서도 distinct 되고 JPA 내부적으로도 같은 식별자를 가진 엔티티 중복을 제거해서 반환한다.
        String qlString = "select distinct t from Team as t inner join fetch t.members where t.name = :teamName";
        List<Team> teams = em.createQuery(qlString, Team.class)
                .setParameter("teamName", "team1")
                .getResultList();

        System.out.println("teams.size() = " + teams.size());
        for (Team team : teams) {
            System.out.println("team = " + team);
            for (Member member : team.getMembers()) {
                System.out.println("member = " + member);
            }
        }
    }

    private static void dontUseAliasAndFilteringCollectionFetchJoin() {
        // 하이버네이트는 페치 조인 대상에 별칭을 줘서 필터링 할 수 있지만 사용하지 말자.
        // 페치 조인은 전체를 가져오겠다는 것인데 일부만 가져와서 컬렉션에 넣으면 cascade가 걸려있으면? 영속성 컨텍스트의 입장에서는?
        // 데이터의 정합성이나 객체 그래프의 사상에 맞지 않다.
//        String qlString = "select distinct t from Team as t inner join fetch t.members as m where t.name = :teamName and m.age <= 50";
        // 여러 단계 fetch 할때 정도만 사용하자.
        String qlString = "select t from Team as t inner join fetch t.members as m where t.name = :teamName";
        List<Team> teams = em.createQuery(qlString, Team.class)
                .setParameter("teamName", "team1")
                .getResultList();

        System.out.println("teams.size() = " + teams.size());
        for (Team team : teams) {
            System.out.println("team = " + team);
            for (Member member : team.getMembers()) {
                System.out.println("member = " + member);
            }
        }
    }

    private static void collectionFetchJoinPagingAlternative() {
        // 하이버네이트는 경고 로그를 남기고 메모리에서 페이징, 매우 위험
        // WARN: HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!
//        String qlString = "select t from Team as t inner join fetch t.members as m where t.id < :teamId";
        // 대안
        // 1. 다대일로 방향을 뒤집는다.
//        String qlString = "select m from Member as m inner join fetch m.team where m.team.id < :teamId";
        // 2. 컬렉션에 @BatchSize(size = 1000) 하면 지연 로딩을 여러번하지 않고 in 으로 batch size 만큼 한번에 한다.
        String qlString = "select t from Team as t where t.id < :teamId";
        // 3. 글로벌 설정에 <property name="hibernate.default_batch_fetch_size" value="1000" />

        List<Team> teams = em.createQuery(qlString, Team.class)
                .setParameter("teamId", 6L)
                .setFirstResult(0)
                .setMaxResults(5)
                .getResultList();

        System.out.println("teams.size() = " + teams.size());
        for (Team team : teams) {
            System.out.println("team = " + team);
            for (Member member : team.getMembers()) {
                System.out.println("member = " + member);
            }
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
