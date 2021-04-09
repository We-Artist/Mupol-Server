package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.UserService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Api(tags = {"1. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ResponseService responseService;
    private final JwtTokenProvider jwtTokenProvider;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 리스트 조회", notes = "모든 회원을 조회한다")
    @GetMapping("/")
    public ResponseEntity<ListResult<User>> findAllUser(){
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult( userRepository.findAll()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
    @GetMapping("/{userId}")
    public ResponseEntity<SingleResult<User>> findUserById(@ApiParam(value = "회원 ID", required = true) @PathVariable long userId) {
        User user = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "본인 프로필 가져오기")
    @GetMapping("/me")
    public ResponseEntity<SingleResult<User>> getMyProfile(@RequestHeader("Authorization") String jwt) {
        User user = userRepository.findById(Long.valueOf(jwtTokenProvider.getUserPk(jwt))).orElseThrow(IllegalArgumentException::new);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "본인 계정 삭제")
    @DeleteMapping("/me")
    public ResponseEntity<SingleResult<String>> deleteAccount(@RequestHeader("Authorization") String jwt) {
        userRepository.deleteById(Long.valueOf(jwtTokenProvider.getUserPk(jwt)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }
}
