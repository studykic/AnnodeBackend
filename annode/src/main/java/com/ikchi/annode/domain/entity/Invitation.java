package com.ikchi.annode.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invitation")
@Getter
@Setter
@NoArgsConstructor
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long id;

    @Column(name = "invitation_code")
    private String invitationCode;

    @ManyToOne
    @JoinColumn(name = "inviter_user_id")
    private User inviterUser;

    @OneToOne
    @JoinColumn(name = "invitee_user_id")
    private User inviteeUser;

    @JoinColumn(name = "invitation_success_count")
    private int invitationSuccessCount;

    public Invitation(User inviterUser) {
        UUID uuid = UUID.randomUUID();
        String rawString = Long.toString(Math.abs(uuid.getMostSignificantBits()), 36);
        this.invitationCode = rawString.substring(0, Math.min(rawString.length(), 8));
        this.inviterUser = inviterUser;
    }


}
