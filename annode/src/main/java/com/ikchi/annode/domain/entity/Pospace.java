package com.ikchi.annode.domain.entity;

import com.ikchi.annode.domain.Constants.Visibility;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "pospace")
public class Pospace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pospace_id")
    private Long id;

    // 방 입장 토큰은 16자리의 UUID를 사용하며 방 생성시 자동으로 생성된다
    @Column(name = "pospace_token", unique = true)
    @Size(max = 16)
    private String pospaceToken;

    //    한명의 유저는 여러개의 게시글을 작성할수있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @Column(name = "pospace_content")
    @Size(max = 300, message = "300자 이내로 작성해주세요")
    private String pospaceContent;

    @Column(name = "max_annode")
    private Integer maxAnnode;

    @Version
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "tolk_open")
    private Boolean tolkOpen;


    @ElementCollection
    @CollectionTable(name = "pospace_image_url", joinColumns = @JoinColumn(name = "pospace_id"))
    @Column(name = "image_file_url")
    private List<String> pospaceImgFileUrlList;

    // 진짜 User엔티티로 일대다로 바꾸기
    @ElementCollection
    @CollectionTable(name = "pospace_user_tag", joinColumns = @JoinColumn(name = "pospace_id"))
    @Column(name = "user_tag")
    private List<String> pospaceUserTagList;


    // 진짜 User엔티티로 일대다로 바꾸기
    @ElementCollection
    @CollectionTable(name = "pospace_like_users", joinColumns = @JoinColumn(name = "pospace_id"))
    @Column(name = "user_identifier")
    private List<String> pospaceLikeUsers;

    @OneToMany(mappedBy = "pospace", cascade = CascadeType.REMOVE)
    private List<PospaceComment> pospaceCommentList;


    public Pospace(User writer, String pospaceContent, Integer maxAnnode, Visibility visibility,
        Boolean tolkOpen,
        List<String> pospaceImgFileUrlList) {
        this.writer = writer;
        this.pospaceContent = pospaceContent;
        this.maxAnnode = maxAnnode;
        this.visibility = visibility;
        this.tolkOpen = tolkOpen;
        this.pospaceImgFileUrlList = pospaceImgFileUrlList;
        this.createdTime = createdTime = LocalDateTime.now();

        int pospaceTokenLength = 16;
        String uuid = UUID.randomUUID().toString();
        this.pospaceToken = uuid.substring(0, Math.min(uuid.length(), pospaceTokenLength));
    }


}
