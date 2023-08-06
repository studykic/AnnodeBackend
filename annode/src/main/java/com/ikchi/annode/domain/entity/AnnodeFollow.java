package com.ikchi.annode.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "annode_follow")
@NoArgsConstructor
@Getter
@ToString
public class AnnodeFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "annode_follow_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;  // 요청을 보낸 사용자

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;  // 요청을 받는 사용자

    public AnnodeFollow(User requester, User receiver) {
        this.requester = requester;
        this.receiver = receiver;
    }
}
