package com.mupol.mupolserver.domain.report;

import com.mupol.mupolserver.domain.common.ReportType;
import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private ReportType type;
    private String email;
    private String content;
}
