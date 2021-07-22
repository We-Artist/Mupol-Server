package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.common.ReportType;
import com.mupol.mupolserver.domain.common.ReportVideoType;
import com.mupol.mupolserver.domain.hashtag.Hashtag;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.report.Report;
import com.mupol.mupolserver.domain.report.ReportRepository;
import com.mupol.mupolserver.domain.reportVideo.ReportVideo;
import com.mupol.mupolserver.domain.reportVideo.ReportVideoRepository;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.common.ReportDto;
import com.mupol.mupolserver.dto.common.ReportVideoDto;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.UserService;
import com.mupol.mupolserver.service.VideoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    private final ReportVideoRepository reportVideoRepository;
    private final UserService userService;
    private final VideoService videoService;

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
            report.put("id", r.getId().toString());
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
        ReportType t = null;
        for(ReportType rt: ReportType.values()) {
            if(rt.getId().equals(dto.getType())) {
                t = rt;
            }
        }
        if(t == null) {
            throw new IllegalArgumentException("invalid report type");
        }

        reportRepository.save(Report.builder()
                .type(t)
                .title(dto.getTitle())
                .email(dto.getEmail())
                .content(dto.getContent())
                .build());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("reported"));
    }

    @ApiOperation(value = "비디오 신고 타입 조회")
    @GetMapping("/report/video/values")
    public ResponseEntity<ListResult<HashMap<String, String>>> getVideoReportType() {
        ReportVideoType[] reportVideoTypes = ReportVideoType.values();
        List<HashMap<String, String>> keywords = new ArrayList<>();
        for (ReportVideoType rv : reportVideoTypes) {
            HashMap<String, String> report = new HashMap<>();
            report.put("id", rv.name());
            report.put("valueKr", rv.getKo());
            keywords.add(report);
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(keywords));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 신고하기")
    @PostMapping(value = "/report/video")
    public ResponseEntity<SingleResult<String>> unblock(
            @RequestHeader("Authorization") String jwt,
            @RequestBody ReportVideoDto dto
    ) throws IOException, InterruptedException {
        User reporter = userService.getUserByJwt(jwt);

        ReportVideoType rv;
        try {
            rv = ReportVideoType.valueOf(dto.getType());
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid report video type");
        }

        reportVideoRepository.save(ReportVideo.builder()
                .reportVideoType(rv)
                .reporter(reporter)
                .content(dto.getReportVideoContent())
                .reportedVid(videoService.getVideo(dto.getReportedVidId()))
                .build());

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("reported video"));
    }

}
