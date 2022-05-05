package com.give928.jpa.basic.domain;

import javax.persistence.*;

@Entity
public class Locker extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "locker_id")
    private Long id;

    @Column(name = "locker_no")
    private int lockerNo;

    @OneToOne(mappedBy = "locker")
    private Member member;
}
