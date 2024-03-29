package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.domain.follower.Follower;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.notification.TargetType;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.user.*;
import com.mupol.mupolserver.service.*;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Api(tags = {"1. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/user")
public class UserController {

    private final UserService userService;
    private final FollowerService followerService;
    private final ResponseService responseService;
    private final NotificationService notificationService;
    private final S3Service s3Service;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = false, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 리스트 조회", notes = "모든 회원을 조회한다")
    @GetMapping("/all")
    public ResponseEntity<ListResult<UserResDto>> findAllUser() {
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(userService.getAllUserDtos()));
    }

    @ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
    @GetMapping("/{userId}")
    public ResponseEntity<SingleResult<UserResDto>> findUserById(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @ApiParam(value = "회원 ID", required = true) @PathVariable long userId
    ) {
        User user = userService.getUserByJwt(jwt);
        User target = userService.getUserById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(userService.getDto(user, target)));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "본인 계정 조회")
    @GetMapping("/me")
    public ResponseEntity<SingleResult<UserResDto>> getMyProfile(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserByJwt(jwt);
        UserResDto dto = userService.getDto(null, user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "본인 계정 삭제")
    @DeleteMapping("/me")
    public ResponseEntity<SingleResult<String>> deleteAccount(@RequestHeader("Authorization") String jwt) {
        userService.delete(userService.getUserByJwt(jwt));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "프로필 이미지 변경", notes = "profileImage를 null로 처리하면 user의 profileImage는 삭제됩니다.")
    @PostMapping("/me/profile-image")
    public ResponseEntity<SingleResult<UserResDto>> updateProfileImage(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "이미지 파일") @RequestParam(required = false) MultipartFile profileImage
    ) throws IOException {
        User user = userService.getUserByJwt(jwt);

        if (profileImage == null || profileImage.isEmpty()) {
            user.setProfileImageUrl(null);
        } else {
            String profileImageUrl = s3Service.uploadProfileImage(profileImage, user.getId());
            user.setProfileImageUrl(profileImageUrl);
        }
        userService.save(user);
        UserResDto dto = userService.getDto(null, user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "프로필 배경 이미지 변경", notes = "빈 파일을 업로드하면 등록된 이미지가 삭제됩니다.")
    @PostMapping("/me/profile-bg-image")
    public ResponseEntity<SingleResult<UserResDto>> updateProfileBgImage(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "이미지 파일") @RequestParam(required = false) MultipartFile profileBgImage
    ) throws IOException {
        User user = userService.getUserByJwt(jwt);

        if (profileBgImage == null || profileBgImage.isEmpty()) {
            user.setProfileImageUrl(null);
        } else {
            String profileBgImageUrl = s3Service.uploadProfileBgImage(profileBgImage, user.getId());
            user.setProfileBgImageUrl(profileBgImageUrl);
        }
        userService.save(user);
        UserResDto dto = userService.getDto(null, user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "프로필 데이터 수정")
    @PostMapping("/me")
    public ResponseEntity<SingleResult<UserResDto>> updateProfile(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "프로필 정보") @RequestBody ProfileUpdateReqDto dto
    ) {
        User user = userService.getUserByJwt(jwt);

        List<String> instruments = dto.getInstruments();
        List<Instrument> instrumentList = new ArrayList<>();

        if (instruments != null) {
            try {
                for (String inst : instruments) instrumentList.add(Instrument.valueOf(inst));
            } catch (Exception e) {
                throw new InstrumentNotExistException();
            }
        }

        if (!userService.validateUsername(dto.getUsername()))
            throw new IllegalArgumentException("올바르지 않은 이름입니다.");

        user.setBio(dto.getBio());
        user.setUsername(dto.getUsername());
        user.setFavoriteInstrument(instrumentList);

        userService.save(user);
        UserResDto userResDto = userService.getDto(null, user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(userResDto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "유저 팔로우")
    @PostMapping("/friendship/follow/{userId}")
    public ResponseEntity<SingleResult<String>> follow(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "user to follow") @PathVariable String userId
    ) throws IOException {
        long followingId = Long.parseLong(userId);

        User from = userService.getUserByJwt(jwt);
        User to = userService.getUserById(followingId);

        if (followerService.isFollowingUser(from, to))
            throw new IllegalArgumentException("already followed");

        boolean isFollowEachOther = followerService.isFollowingUser(to, from);
        Follower follower = Follower.builder()
                .from(from)
                .to(to)
                .isFollowEachOther(isFollowEachOther)
                .build();

        if(isFollowEachOther) {
            Follower following = followerService.getFollowerByFromAndTo(to, from);
            following.setFollowEachOther(true);
            followerService.save(following);
        }
        followerService.save(follower);

        notificationService.send(
                from,
                to,
                from,
                isFollowEachOther,
                TargetType.follow
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("success follow"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "유저 언팔로우")
    @PostMapping("/friendship/unfollow/{userId}")
    public ResponseEntity<SingleResult<String>> unfollow(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "user to unfollow") @PathVariable String userId
    ) {
        long followingId = Long.parseLong(userId);

        User from = userService.getUserByJwt(jwt);
        User to = userService.getUserById(followingId);

        if(followerService.isFollowingUser(to, from)) {
            Follower follower = followerService.getFollowerByFromAndTo(to, from);
            follower.setFollowEachOther(false);
            followerService.save(follower);
        }
        followerService.delete(followerService.getFollowerByFromAndTo(from, to));

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("success unfollow"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "팔로워 목록 불러오기")
    @GetMapping("/{userId}/followers")
    public ResponseEntity<ListResult<FollowerResDto>> getFollowers(
            @ApiParam(value = "user id") @PathVariable Long userId
    ) {
        User user = userService.getUserById(userId);
        List<FollowerResDto> dtoList = followerService.getFollowerDtoList(user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "팔로잉 목록 불러오기")
    @GetMapping("/{userId}/followings")
    public ResponseEntity<ListResult<FollowingResDto>> getFollowings(
            @ApiParam(value = "user id") @PathVariable Long userId
    ) {
        User user = userService.getUserById(userId);
        List<FollowingResDto> dtoList = followerService.getFollowingDtoList(user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "알림 설정")
    @PostMapping("/me/notification")
    public ResponseEntity<SingleResult<String>> setNotification(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "true or false") @RequestBody NotiSettingReqDto dto
    ) {
        User user = userService.getUserByJwt(jwt);
        user.setNotificationAllowed(dto.getIsNotiAllowed());
        userService.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("notification setting: " + dto.getIsNotiAllowed().toString()));
    }

    @ApiOperation(value = "닉네임 벨리데이션 체크")
    @PostMapping("/validate-name")
    public ResponseEntity<SingleResult<Boolean>> updateProfile(
            @ApiParam(value = "username") @RequestBody NicknameValidateReqDto dto
    ) {
        if (!userService.validateUsername(dto.getUsername())) throw new IllegalArgumentException("올바르지 않은 이름입니다.");
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(true));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "fcm token 등록")
    @PatchMapping("/fcm-token")
    public ResponseEntity<SingleResult<String>> registerFcmToken(
            @RequestHeader("Authorization") String jwt,
            @RequestBody FcmTokenReqDto dto
    ) {
        User user = userService.getUserByJwt(jwt);
        userService.registerFcmToken(user, dto.getToken());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("fcm token is registered"));
    }
}
