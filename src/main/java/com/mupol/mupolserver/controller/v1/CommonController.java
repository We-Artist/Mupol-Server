package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.common.ReportType;
import com.mupol.mupolserver.domain.hashtag.Hashtag;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.report.Report;
import com.mupol.mupolserver.domain.report.ReportRepository;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.dto.common.ReportDto;
import com.mupol.mupolserver.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Api(tags = {"Common"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/common")
public class CommonController {

    private final ReportRepository reportRepository;
    private final ResponseService responseService;

    @ApiOperation(value = "악기 리스트 조회")
    @GetMapping("/instr")
    public ResponseEntity<ListResult<HashMap<String, String>>> getInstruments() {
        Instrument[] instrEn = Instrument.values();
        List<HashMap<String, String>> instrumentsList = new ArrayList<>();
        for (Instrument instrument : instrEn) {
            HashMap<String, String> instr = new HashMap<>();
            instr.put("id", instrument.getEn());
            instr.put("valueKr", instrument.getKo());
            instrumentsList.add(instr);
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(instrumentsList));
    }

    @ApiOperation(value = "해시태그 조회")
    @GetMapping("/feedback")
    public ResponseEntity<ListResult<HashMap<String, String>>> getHashtags() {
        Hashtag[] hashtags = Hashtag.values();
        List<HashMap<String, String>> keywords = new ArrayList<>();
        for (Hashtag h : hashtags) {
            HashMap<String, String> hashtag = new HashMap<>();
            hashtag.put("id", h.getEn());
            hashtag.put("valueKr", h.getKo());
            keywords.add(hashtag);
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(keywords));
    }

    @ApiOperation(value = "리폿 타입 조회")
    @GetMapping("/report/values")
    public ResponseEntity<ListResult<HashMap<String, String>>> getReportType() {
        ReportType[] reportTypes = ReportType.values();
        List<HashMap<String, String>> keywords = new ArrayList<>();
        for (ReportType r : reportTypes) {
            HashMap<String, String> report = new HashMap<>();
            report.put("id", r.name());
            report.put("valueKr", r.getKo());
            keywords.add(report);
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(keywords));
    }

    @ApiOperation(value = "버그 리포트")
    @PostMapping("/report/bug")
    public ResponseEntity<SingleResult<String>> reportBug(
            @RequestBody ReportDto dto
    ) {
        ReportType t;
        try {
            t = ReportType.valueOf(dto.getType());
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid report type");
        }

        reportRepository.save(Report.builder()
                .type(t)
                .title(dto.getTitle())
                .email(dto.getEmail())
                .name(dto.getName())
                .content(dto.getContent())
                .build());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("reported"));
    }
}
