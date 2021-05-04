package com.mupol.mupolserver.controller.v1;


import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoalRepository;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.monthlyGoal.CreateGoalReqDto;
import com.mupol.mupolserver.dto.monthlyGoal.GoalStatusReqDto;
import com.mupol.mupolserver.dto.sound.SoundResDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.SoundService;
import com.mupol.mupolserver.service.VideoService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Api(tags = {"Monthly Goal"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/goal")
public class MonthlyGoalController {

    private final MonthlyGoalRepository monthlyGoalRepository;
    private final UserRepository userRepository;
    private final VideoService videoService;
    private final SoundService soundService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "월 목표 생성")
    @PostMapping("/new")
    public ResponseEntity<SingleResult<MonthlyGoal>> createGoal(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "목표 횟수") @RequestBody CreateGoalReqDto dto
    ) {
        // get user
        long userId = Long.parseLong(jwtTokenProvider.getUserPk(jwt));
        User user = userRepository.findById(userId).orElseThrow(CUserNotFoundException::new);
        int y = LocalDate.now().getYear();
        int m = LocalDate.now().getMonthValue();
        LocalDate startDate = LocalDate.of(y, m, 1);

        // check goal exist
        Optional<MonthlyGoal> goal = monthlyGoalRepository.findMonthlyGoalByUserAndStartDate(user, startDate);
        if (goal.isPresent()) {
            throw new IllegalArgumentException("goal already exist");
        }

        // create goal
        MonthlyGoal monthlyGoal = MonthlyGoal.builder()
                .user(user)
                .startDate(startDate)
                .goalNumber(dto.getGoalNum())
                .achieveNumber(0)
                .build();

        monthlyGoalRepository.save(monthlyGoal);

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(monthlyGoal));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "목표 달성 상태 불러오기")
    @GetMapping("/{year}/{month}")
    public ResponseEntity<SingleResult<GoalStatusReqDto>> getGoal(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "year") @PathVariable int year,
            @ApiParam(value = "month") @PathVariable int month
    ) {
        // get user
        long userId = Long.parseLong(jwtTokenProvider.getUserPk(jwt));
        User user = userRepository.findById(userId).orElseThrow(CUserNotFoundException::new);

        LocalDate startDate = LocalDate.of(year, month, 1);

        // check goal exist
        MonthlyGoal goal = monthlyGoalRepository.findMonthlyGoalByUserAndStartDate(user, startDate)
                .orElseThrow(() -> new IllegalArgumentException("goal does not exist"));

        // get video
        List<VideoResDto> videoList = videoService.getVideoAtMonth(user, year, month);

        // get sound
        List<SoundResDto> soundList = soundService.getSoundAtMonth(user, year, month);

        GoalStatusReqDto dto = GoalStatusReqDto.builder()
                .currentGoal(goal)
                .videoList(videoList)
                .soundList(soundList)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "전체 목표 불러오기")
    @GetMapping("/all")
    public ResponseEntity<ListResult<MonthlyGoal>> getAllGoals(
            @RequestHeader("Authorization") String jwt
    ) {
        long userId = Long.parseLong(jwtTokenProvider.getUserPk(jwt));
        User user = userRepository.findById(userId).orElseThrow(CUserNotFoundException::new);

        List<MonthlyGoal> goalList = monthlyGoalRepository.findAllByUserOrderByStartDateDesc(user)
                .orElseThrow(() -> new IllegalArgumentException("goals do not exist"));

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(goalList));
    }

}
