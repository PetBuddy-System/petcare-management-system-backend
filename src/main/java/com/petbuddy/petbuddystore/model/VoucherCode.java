package com.petbuddy.petbuddystore.model;

import com.petbuddy.petbuddystore.common.enums.CodeStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "voucher_code")
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Enumerated(EnumType.STRING)
    private CodeStatus status;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;
}
