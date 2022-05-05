package com.give928.jpa.basic.domain;

import com.give928.jpa.basic.domain.embeddable.Address;
import com.give928.jpa.basic.domain.embeddable.WorkPeriod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Getter
@NamedQuery(
        name = "Member.findByUsername",
        query = "select m from Member as m where m.username = :username"
)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @Embedded
    private WorkPeriod workPeriod;

    @Embedded
    @AttributeOverride(name = "city", column = @Column(name = "home_city"))
    @AttributeOverride(name = "street", column = @Column(name = "home_street"))
    @AttributeOverride(name = "zipcode", column = @Column(name = "home_zipcode"))
    private Address homeAddress;

    @Embedded
    @AttributeOverride(name = "city", column = @Column(name = "work_city"))
    @AttributeOverride(name = "street", column = @Column(name = "work_street"))
    @AttributeOverride(name = "zipcode", column = @Column(name = "work_zipcode"))
    private Address workAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "member") // Order 엔티티의 member 를 연관관계 주인으로 지정
    private List<Order> orders = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id")
    private Locker locker;

    @ElementCollection
    @CollectionTable(name = "favorite_food", joinColumns = {@JoinColumn(name = "member_id")})
    @Column(name = "food_name")
    private Set<String> favoriteFoods = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "address_history_embed", joinColumns = {@JoinColumn(name = "member_id")})
    @OrderColumn(name = "address_history_order")
    private List<Address> addressHistory = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "member_id")
    private List<AddressHistory> addressHistories = new ArrayList<>();

    public void changeUsername(String username) {
        this.username = username;
    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", age=" + age +
                ", workPeriod=" + workPeriod +
                ", homeAddress=" + homeAddress +
                ", workAddress=" + workAddress +
                ", roleType=" + roleType +
//                ", team=" + team +
//                ", team=" + team.getName() +
//                ", orders=" + orders +
//                ", locker=" + locker +
//                ", favoriteFoods=" + favoriteFoods +
//                ", addressHistory=" + addressHistory +
//                ", addressHistories=" + addressHistories +
                '}';
    }
}
