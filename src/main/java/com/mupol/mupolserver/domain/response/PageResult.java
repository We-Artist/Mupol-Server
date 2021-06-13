package com.mupol.mupolserver.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageResult<T> extends ListResult{
    @ApiModelProperty(value = "이전 페이지 존재여부 : true/false")
    private boolean hasPrev;

    @ApiModelProperty(value = "다음 페이지 존재여부 : true/false")
    private boolean hasNext;
}
