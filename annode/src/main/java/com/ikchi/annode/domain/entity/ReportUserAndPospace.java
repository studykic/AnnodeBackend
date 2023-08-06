package com.ikchi.annode.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class ReportUserAndPospace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Size(max = 1000)
    @Column(name = "report_reason")
    private String reportReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pospace_id")
    private Pospace pospace;

    @Column(name = "report_date")
    private LocalDateTime reportDate;

    public ReportUserAndPospace(User reporter, String reportReason, Pospace pospace) {
        this.reporter = reporter;
        this.reportReason = reportReason;
        this.pospace = pospace;
        this.reportDate = LocalDateTime.now();
    }
}
