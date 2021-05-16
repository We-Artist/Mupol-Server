package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.notification.NotiReadReqDto;
import com.mupol.mupolserver.dto.notification.NotificationDto;
import com.mupol.mupolserver.dto.notification.UnreadNotificationNumberDto;
import com.mupol.mupolserver.service.NotificationService;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.UserService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = {"Notification"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "받은 알림 목록")
    @GetMapping(value = "/list")
    public ResponseEntity<ListResult<NotificationDto>> getReceivedNotifications(
            @RequestHeader("Authorization") String jwt
    ) {
        User user = userService.getUserByJwt(jwt);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseService.getListResult(notificationService.getReceivedNotification(user)));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "받은 알림 목록 (20개씩)")
    @GetMapping(value = "/list/{page}")
    public ResponseEntity<ListResult<NotificationDto>> getReceivedNotifications(
            @RequestHeader("Authorization") String jwt,
            @PathVariable int page
    ) {
        User user = userService.getUserByJwt(jwt);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseService.getListResult(notificationService.getReceivedNotification(user, page)));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "읽지 않은 알림 수")
    @GetMapping(value = "/unread")
    public ResponseEntity<SingleResult<UnreadNotificationNumberDto>> getUnreadNotiNum(
            @RequestHeader("Authorization") String jwt
    ) {
        User user = userService.getUserByJwt(jwt);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseService.getSingleResult(notificationService.getUnreadNotificationNumber(user)));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "개별 알림 읽기")
    @PostMapping(value = "/read")
    public ResponseEntity<SingleResult<String>> readNotification(
            @RequestHeader("Authorization") String jwt,
            @RequestBody NotiReadReqDto dto
    ) {
        notificationService.readNotification(dto.getId(), userService.getUserByJwt(jwt));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseService.getSingleResult("read success"));
    }
}
