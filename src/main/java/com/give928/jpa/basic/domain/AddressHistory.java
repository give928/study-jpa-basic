package com.give928.jpa.basic.domain;

import com.give928.jpa.basic.domain.embeddable.Address;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Getter
@ToString
public class AddressHistory extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "address_id")
    private Long id;

    @Embedded
    private Address address;
}
