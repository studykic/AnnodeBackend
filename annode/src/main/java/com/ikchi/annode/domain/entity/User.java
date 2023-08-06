package com.ikchi.annode.domain.entity;

import com.ikchi.annode.domain.vo.Authority;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_identifier", unique = true)
    private String userIdentifier;  // 사용자 식별을 위한 고유 코드를 추가합니다.

    @Column(name = "fcm_token", unique = true)
    private String fcmToken;

    private String profileImgFileUrl;

    // 유저간의 닉네임 중복 허용
    private String nickName;

    // unique옵션은 애플리케이션 레벨에서 중복 검사를 하는것이 아닌 DB레벨에서 중복검사를 하기에 효율적임
    @Column(unique = true)
    private String email;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    private String password;

    private boolean accountPublic;

    @Column(name = "joined_room_id")
    private String joinedRoom;

    private LocalDateTime userCreatedDate;

    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private List<Pospace> pospaceList;

    @OneToMany(mappedBy = "commentWriter", cascade = CascadeType.REMOVE)
    private List<PospaceComment> pospaceCommentList;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserRelationShip> userRelationShipList;

    // 유저의 권한 설정
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    private List<Authority> roles = new ArrayList<>();

    // user을 생성한다 이때 기본 권한은 ROLE_USER로 설정한다
    public User(String email, String password, String phoneNumber, String nickName,
        boolean accountPublic) {
        this.userIdentifier = generateId();
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.nickName = nickName;
        this.accountPublic = accountPublic;
        this.userCreatedDate = LocalDateTime.now();
        this.roles.add(new Authority("ROLE_USER"));
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public void adminSetting() {
        this.roles.add(new Authority("ROLE_ADMIN"));
    }

    public Boolean isAdmin() {

        return this.roles.stream().anyMatch(authority -> authority.getName().equals("ROLE_ADMIN"));
    }


    // 편의 메소드
    public void addUserRelationShip(UserRelationShip userRelationShip) {
        this.userRelationShipList.add(userRelationShip);
        userRelationShip.setUser(this);
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", profileImgFileUrl='" + profileImgFileUrl + '\'' +
            ", nickName='" + nickName + '\'' +
            ", email='" + email + '\'' +
            ", password='" + password + '\'' +
            ", userCreatedDate=" + userCreatedDate +
            ", roles=" + roles +
            '}';
    }


}
