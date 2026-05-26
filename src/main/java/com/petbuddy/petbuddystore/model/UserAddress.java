package com.petbuddy.petbuddystore.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    Long addressId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Column(columnDefinition = "NVARCHAR(255)")
    String recipientName;

    String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(100)")
    String province;

    @Column(columnDefinition = "NVARCHAR(100)")
    String district;

    @Column(columnDefinition = "NVARCHAR(100)")
    String ward;

    @Column(columnDefinition = "NVARCHAR(500)")
    String detailAddress;

    Boolean isDefault;
}