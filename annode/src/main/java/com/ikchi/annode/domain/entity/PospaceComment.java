package com.ikchi.annode.domain.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "pospace_comment")
@NoArgsConstructor
public class PospaceComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pospace_comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pospace_id")
    private Pospace pospace;


    @Column(name = "comment_content")
    private String commentContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_writer_id", referencedColumnName = "user_id")
    private User commentWriter;


    public PospaceComment(Pospace pospace, User commentWriter, String commentContent) {
        this.pospace = pospace;
        this.commentWriter = commentWriter;
        this.commentContent = commentContent;
    }
}
