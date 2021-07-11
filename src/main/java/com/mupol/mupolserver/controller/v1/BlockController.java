package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.block.BlockResDto;
import com.mupol.mupolserver.service.BlockService;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.UserService;
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

@Slf4j
@Api(tags = {"Block"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/block")
public class BlockController {

    private final BlockService blockService;
    private final UserService userService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "차단하기", notes = "")
    @PostMapping(value = "/{userId}")
    public ResponseEntity<SingleResult<BlockResDto>> block(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String userId
    ) throws IOException, InterruptedException {
        User blocker = userService.getUserByJwt(jwt);
        User blocked = userService.getUserById(Long.valueOf(userId));

        BlockResDto dto = blockService.block(blocker, blocked);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "차단 해제하기", notes = "")
    @PostMapping(value = "/undo/{userId}")
    public ResponseEntity<SingleResult<String>> unblock(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String userId
    ) throws IOException, InterruptedException {
        User blocker = userService.getUserByJwt(jwt);
        User blocked = userService.getUserById(Long.valueOf(userId));

        blockService.unblock(blocker, blocked);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }


}
