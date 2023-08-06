//package com.ikchi.annode.domain.user;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//
//// 사용자의 권한 엔티티
//@Entity
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class Authority {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;
//
//    @JoinColumn(name = "user_id")
//    @ManyToOne
//    @JsonIgnore
//    private User user;
//
//    public void setMember(User user) {
//        this.user = user;
//    }
//}
package com.ikchi.annode.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사용자의 권한 엔티티

@Getter
@NoArgsConstructor
@Embeddable
public class Authority {

    @Column(name = "role_name")
    private String name;

    public Authority(String name) {
        this.name = name;
    }
}