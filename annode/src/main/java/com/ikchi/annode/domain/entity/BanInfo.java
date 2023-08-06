package com.ikchi.annode.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "ban_info")
public class BanInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_id")
    private Long id;

    private String email;

    private String phoneNumber;

    private LocalDateTime userBanDate;

    public BanInfo(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userBanDate = LocalDateTime.now();
    }
}
