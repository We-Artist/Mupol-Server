package com.mupol.mupolserver.controller.v1;


import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.monthlyGoal.CreateGoalReqDto;
import com.mupol.mupolserver.dto.monthlyGoal.GoalStatusResDto;
import com.mupol.mupolserver.dto.sound.SoundResDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import com.mupol.mupolserver.service.*;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Api(tags = {"Monthly Goal"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/goal")
public class MonthlyGoalController {

    private final ResponseService responseService;
    private final MonthlyGoalService monthlyGoalService;
    private final UserService userService;
    private final VideoService videoService;
    private final SoundService soundService;

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
        User user = userService.getUserByJwt(jwt);
        int y = LocalDate.now().getYear();
        int m = LocalDate.now().getMonthValue();
        LocalDate startDate = LocalDate.of(y, m, 1);

        // check goal exist
        if (monthlyGoalService.isGoalExist(user, startDate)) {
            throw new IllegalArgumentException("goal already exist");
        }

        // create goal
        MonthlyGoal monthlyGoal = MonthlyGoal.builder()
                .user(user)
                .startDate(startDate)
                .goalNumber(dto.getGoalNum())
                .achieveNumber(0)
                .build();

        monthlyGoalService.save(monthlyGoal);

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(monthlyGoal));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "목표 달성 상태 불러오기")
    @GetMapping("/{year}/{month}")
    public ResponseEntity<SingleResult<GoalStatusResDto>> getGoal(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "year") @PathVariable int year,
            @ApiParam(value = "month") @PathVariable int month
    ) {
        // get user
        User user = userService.getUserByJwt(jwt);
        LocalDate startDate = LocalDate.of(year, month, 1);

        // check goal exist
        MonthlyGoal goal = monthlyGoalService.getMonthlyGoal(user, startDate);

        // get video
        List<VideoResDto> videoList = videoService.getVideoAtMonth(user, year, month);

        // get sound
        List<SoundResDto> soundList = soundService.getSoundAtMonth(user, year, month);

        GoalStatusResDto dto = GoalStatusResDto.builder()
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
        User user = userService.getUserByJwt(jwt);
        List<MonthlyGoal> goalList = monthlyGoalService.getAllGoals(user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(goalList));
    }

}
