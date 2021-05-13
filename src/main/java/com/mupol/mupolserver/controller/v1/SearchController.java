package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.dto.search.SearchResultDto;
import com.mupol.mupolserver.dto.search.SuggestionResultDto;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.SearchService;
import com.mupol.mupolserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = {"Search"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/search")
public class SearchController {

    private final SearchService searchService;
    private final UserService userService;
    private final ResponseService responseService;

    @ApiOperation(value = "검색", notes = "")
    @GetMapping(value = "/{keyword}")
    public ResponseEntity<SingleResult<SearchResultDto>> search(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @ApiParam(value = "검색 키워드") @PathVariable String keyword
    ) {
        SearchResultDto dto;
        if (jwt == null) dto = searchService.getSearchResult(null, keyword);
        else dto = searchService.getSearchResult(userService.getUserByJwt(jwt), keyword);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiOperation(value = "자동완성", notes = "")
    @GetMapping(value = "/autocomplete/{keyword}")
    public ResponseEntity<SingleResult<SuggestionResultDto>> search(
            @ApiParam(value = "검색 키워드") @PathVariable String keyword
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(searchService.getSuggestion(keyword)));
    }

    @ApiOperation(value = "인기 검색어 3개", notes = "")
    @GetMapping(value = "/hot/keyword")
    public ResponseEntity<ListResult<String>> search(
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(searchService.getHotKeyword()));
    }
}
