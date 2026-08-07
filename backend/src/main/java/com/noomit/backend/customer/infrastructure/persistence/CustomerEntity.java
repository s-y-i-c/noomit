package com.noomit.backend.customer.infrastructure.persistence;

import com.noomit.backend.customer.domain.Customer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "customer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "memo")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Customer.Status status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Builder
    public CustomerEntity(String name, String phoneNumber, String zipCode,
                          String address, String detailAddress, String memo) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.memo = memo;
        this.status = Customer.Status.ACTIVE;
    }

    // 같은 phone_number로 다시 접수됐을 때 최신 정보로 갱신하고 재활성화한다.
    // (비활성 상태였던 고객이 다시 전화해도 새 row가 아니라 이 row가 그대로 이어진다)
    public void reactivateWith(String name, String zipCode, String address, String detailAddress, String memo) {
        this.name = name;
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.memo = memo;
        this.status = Customer.Status.ACTIVE;
    }

    public Customer toDomain() {
        return new Customer(id, name, phoneNumber, zipCode, address, detailAddress, memo, status, createdAt);
    }
}
