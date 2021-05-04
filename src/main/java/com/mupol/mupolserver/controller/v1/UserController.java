package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.followers.Followers;
import com.mupol.mupolserver.domain.followers.FollowersRepository;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.user.FollowersResDto;
import com.mupol.mupolserver.dto.user.NicknameValidateReqDto;
import com.mupol.mupolserver.dto.user.ProfileUpdateReqDto;
import com.mupol.mupolserver.service.FollowersService;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.S3Service;
import com.mupol.mupolserver.service.UserService;
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
    private final FollowersService followersService;
    private final ResponseService responseService;
    private final S3Service s3Service;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 리스트 조회", notes = "모든 회원을 조회한다")
    @GetMapping("/")
    public ResponseEntity<ListResult<User>> findAllUser() {
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(userService.getAllUser()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
    @GetMapping("/{userId}")
    public ResponseEntity<SingleResult<User>> findUserById(@ApiParam(value = "회원 ID", required = true) @PathVariable long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "본인 계정 조회")
    @GetMapping("/me")
    public ResponseEntity<SingleResult<User>> getMyProfile(@RequestHeader("Authorization") String jwt) {
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(userService.getUserByJwt(jwt)));
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
    public ResponseEntity<SingleResult<User>> updateProfileImage(
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
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "프로필 데이터 수정")
    @PostMapping("/me")
    public ResponseEntity<SingleResult<User>> updateProfile(
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

        if(!userService.validateUsername(dto.getUsername()))
            throw new IllegalArgumentException("올바르지 않은 이름입니다.");

        user.setBio(dto.getBio());
        user.setUsername(dto.getUsername());
        user.setFavoriteInstrument(instrumentList);

        userService.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "유저 팔로우")
    @PostMapping("/friendship/follow/{userId}")
    public ResponseEntity<SingleResult<Followers>> follow(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "user to follow") @PathVariable String userId
    ) {
        long followingId = Long.parseLong(userId);

        User follower = userService.getUserByJwt(jwt);
        User following = userService.getUserById(followingId);

        if(followersService.isAlreadyFollowed(follower, following))
            throw new IllegalArgumentException("already followed");

        Followers followers = Followers.builder()
                .from(follower)
                .to(following)
                .build();
        followersService.save(followers);

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(followers));
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

        User follower = userService.getUserByJwt(jwt);
        User following = userService.getUserById(followingId);
        followersService.delete(followersService.getFollowersByFromAndTo(follower, following));

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("success unfollow"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "팔로워 목록 불러오기")
    @GetMapping("/{userId}/followers")
    public ResponseEntity<ListResult<FollowersResDto>> getFollowers(
            @ApiParam(value = "user id") @PathVariable Long userId
    ) {
        User user = userService.getUserById(userId);
        List<Followers> followersList = followersService.getFollowersList(user);
        List<FollowersResDto> dtoList = followersService.getFollowersDtoList(followersList);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "팔로잉 목록 불러오기")
    @GetMapping("/{userId}/followings")
    public ResponseEntity<ListResult<FollowersResDto>> getFollowings(
            @ApiParam(value = "user id") @PathVariable Long userId
    ) {
        User user = userService.getUserById(userId);
        List<Followers> followingList = followersService.getFollowingList(user);
        List<FollowersResDto> dtoList = followersService.getFollowingDtoList(followingList);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiOperation(value = "닉네임 벨리데이션 체크")
    @PostMapping("/validate-name")
    public ResponseEntity<SingleResult<Boolean>> updateProfile(
            @ApiParam(value = "username") @RequestBody NicknameValidateReqDto dto
    ) {
        if(!userService.validateUsername(dto.getUsername())) throw new IllegalArgumentException("올바르지 않은 이름입니다.");
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(true));
    }
}
