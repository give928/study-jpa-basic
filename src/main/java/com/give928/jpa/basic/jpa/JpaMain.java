package com.give928.jpa.basic.jpa;

import com.give928.jpa.basic.domain.AddressHistory;
import com.give928.jpa.basic.domain.Book;
import com.give928.jpa.basic.domain.Member;
import com.give928.jpa.basic.domain.Team;
import com.give928.jpa.basic.domain.cascade.Child;
import com.give928.jpa.basic.domain.cascade.Parent;
import com.give928.jpa.basic.domain.embeddable.Address;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;

public class JpaMain {
    public static void main(String[] args) {
//        insert();
//        update();
//        list();
//        relation();

//        inheritance();
//        proxy();
//        lazy();
//        nPlus1();
//        nPlus1Resolve();
//        cascade();
//        embedded();
//        valueTypeCollection();
//        valueTypeCollectionToEntity();

//        jpqlBasic();
//        jpqlCriteria();
        nativeSql();
    }

    private static void nativeSql() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = Member.builder()
                    .username("kim1")
                    .build();

            em.persist(member);

            List members = em.createNativeQuery("select * from member where username like '%kim%'", Member.class)
                    .getResultList();
            System.out.println("members = " + members);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void jpqlCriteria() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // 검색 조건
            Map<String, Object> params = new HashMap<>();
            params.put("username", "kim");

            // Criteria 사용 준비
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Member> query = cb.createQuery(Member.class);

            // 루트 클래스 (조회를 시작할 클래스)
            Root<Member> m = query.from(Member.class);

            // 쿼리 생성
            CriteriaQuery<Member> cq = query.select(m);
            if (params.get("username") != null) {
                cq = cq.where(cb.equal(m.get("username"), params.get("username")));
            }

            List<Member> members = em.createQuery(cq).getResultList();
            System.out.println("members = " + members);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void jpqlBasic() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            String sql = "select m from Member as m where m.username like '%kim%'";
            List<Member> members = em.createQuery(sql, Member.class)
                    .getResultList();
            for (Member m : members) {
                System.out.println("m = " + m);
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void valueTypeCollectionToEntity() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Address address3 = Address.builder().city("서울시").street("문래로3").zipcode("12345").build();

            Member member = Member.builder()
                    .username("member1")
                    .homeAddress(Address.builder().city("서울시").street("문래로").zipcode("12345").build())
                    .workAddress(Address.builder().city("부산시").street("해운대로").zipcode("67890").build())
                    .favoriteFoods(new HashSet<>(Arrays.asList("치킨", "족발", "피자")))
                    .addressHistories(new ArrayList<>(Arrays.asList(
                            AddressHistory.builder().address(
                                    Address.builder().city("서울시").street("문래로1").zipcode("23456").build()).build(),
                            AddressHistory.builder().address(
                                    Address.builder().city("서울시").street("문래로2").zipcode("23456").build()).build(),
                            AddressHistory.builder().address(address3).build()
                    )))
                    .build();

            em.persist(member);

            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());
            System.out.println("findMember = " + findMember);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void valueTypeCollection() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Address address3 = Address.builder().city("서울시").street("문래로3").zipcode("12345").build();

            Member member = Member.builder()
                    .username("member1")
                    .homeAddress(Address.builder().city("서울시").street("문래로").zipcode("12345").build())
                    .workAddress(Address.builder().city("부산시").street("해운대로").zipcode("67890").build())
                    .favoriteFoods(new HashSet<>(Arrays.asList("치킨", "족발", "피자")))
                    .addressHistory(new ArrayList<>(Arrays.asList(
                            Address.builder().city("서울시").street("문래로1").zipcode("23456").build(),
                            Address.builder().city("서울시").street("문래로2").zipcode("23456").build(),
                            address3
                    )))
                    .build();

            em.persist(member);

            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());
            System.out.println("findMember = " + findMember);
//            System.out.println("findMember.getFavoriteFoods() = " + findMember.getFavoriteFoods()); // 지연 로딩
//            System.out.println("findMember.getAddressHistory() = " + findMember.getAddressHistory()); // 지연 로딩

            // 치킨 -> 한식
            findMember.getFavoriteFoods().remove("치킨");
            findMember.getFavoriteFoods().add("한식");

            findMember.getAddressHistory().remove(address3);
            findMember.getAddressHistory().add(Address.builder().city("서울시").street("문래로4").zipcode("23456").build());
            findMember.getAddressHistory().remove(1);

            em.flush();
            em.clear();

            Member findMember2 = em.find(Member.class, member.getId());
            System.out.println("findMember2.getAddressHistory() = " + findMember2.getAddressHistory()); // 지연 로딩

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void embedded() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = Member.builder()
                    .username("member1")
                    .homeAddress(Address.builder().city("서울시").street("문래로").zipcode("12345").build())
                    .workAddress(Address.builder().city("부산시").street("해운대로").zipcode("67890").build())
                    .build();

            em.persist(member);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void cascade() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Child child1 = Child.builder().name("child1").build();
            Child child2 = Child.builder().name("child2").build();

            Parent parent = Parent.builder().name("parent").childList(new ArrayList<>()).build();
            parent.addChild(child1);
            parent.addChild(child2);

            em.persist(parent);
            // @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL) 하면 아래 필요 없다.
//            em.persist(child1);
//            em.persist(child2);

            em.flush();
            em.clear();

            Parent findParent = em.find(Parent.class, parent.getId());
            findParent.getChildList().remove(0);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void nPlus1Resolve() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = Team.builder().name("teamA").build();
            em.persist(team);

            Member member = Member.builder().username("hello").team(team).build();
            em.persist(member);

            em.flush();
            em.clear();

            List<Member> members = em.createQuery("select m from Member m join fetch m.team", Member.class)
                    .getResultList();
            for (Member m : members) {
                System.out.println("m = " + m);
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void nPlus1() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = Team.builder().name("teamA").build();
            em.persist(team);

            Member member = Member.builder().username("hello").team(team).build();
            em.persist(member);

            em.flush();
            em.clear();

            List<Member> members = em.createQuery("select m from Member m", Member.class)
                    .getResultList();
            for (Member m : members) {
                System.out.println("m = " + m);
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void lazy() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = Team.builder().name("teamA").build();
            em.persist(team);

            Member member = Member.builder().username("hello").team(team).build();
            em.persist(member);

            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());
            System.out.println(
                    "findMember.getTeam().getClass() = " + findMember.getTeam().getClass()); // PROXY - class hellojpa.domain.Team$HibernateProxy$KWIWClhq
            System.out.println("====================================");
            System.out.println(
                    "findMember.getTeam().getName() = " + findMember.getTeam().getName()); // 이 시점에 조회 SQL 실행해서 Team 초기화
            System.out.println("====================================");

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void proxy() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = Member.builder().username("hello").build();

            em.persist(member);

            em.flush();
            em.clear();

//            Member findMember = em.find(Member.class, member.getId());
//            System.out.println("findMember.getId() = " + findMember.getId());
//            System.out.println("findMember.getUsername() = " + findMember.getUsername());

            Member findMember = em.getReference(Member.class, member.getId()); // 여기까지는 SQL이 실행되지 않는다.
            System.out.println(
                    "findMember.getClass() = " + findMember.getClass()); // class hellojpa.domain.Member$HibernateProxy$3Slgxl2j
            System.out.println("findMember.getId() = " + findMember.getId());
            System.out.println("findMember.getUsername() = " + findMember.getUsername()); // 사용하는 시점에 SQL이 실행된다.

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void inheritance() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Book book = Book.builder().name("JPA").author("김영한").build();

            em.persist(book);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void relation() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = Team.builder().name("TeamA").build();
            em.persist(team);

            Member member = Member.builder().username("member1").build();
            member.changeTeam(team);
            em.persist(member);

            team.getMembers().add(member);

//            em.flush();
//            em.clear();

            Member findMember = em.find(Member.class, member.getId());
            System.out.println("findMember = " + findMember);
            List<Member> members = findMember.getTeam().getMembers();
            for (Member m : members) {
                System.out.println("m = " + m);
                System.out.println("(m == findMember) = " + (m == findMember));
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void insert() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // 비영속
            Member member = Member.builder().id(1L).username("helloA").build();

            // 영속
            em.persist(member);

            // 1차 캐시에서 조회해서 select 쿼리가 실행되지 않는다.
            Member findMember = em.find(Member.class, 1L);
            System.out.println("findMember = " + findMember);

            // 실제 쿼리가 실행되는 시점
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void update() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member findMember1 = em.find(Member.class, 1L);
            System.out.println("findMember1 = " + findMember1);
            findMember1.changeUsername("helloJPA");

            // 1차 캐시에서 조회해서 select 쿼리가 실행되지 않는다.
            Member findMember2 = em.find(Member.class, 1L);
            System.out.println("findMember2 = " + findMember2);

            // 동일성 보장
            System.out.println("(findMember1 == findMember2) = " + (findMember1 == findMember2));

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    private static void list() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("study-jpa-basic");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            List<Member> members = em.createQuery("select m from Member as m", Member.class)
                    .setFirstResult(0)
                    .setMaxResults(5)
                    .getResultList();
            for (Member member : members) {
                System.out.println("member = " + member);
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
