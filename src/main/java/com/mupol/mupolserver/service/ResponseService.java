package com.mupol.mupolserver.service;


import com.mupol.mupolserver.domain.response.CommonResult;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.PageResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResponseService {

    @AllArgsConstructor
    @Getter
    public enum CommonResponse {
        SUCCESS("성공했습니다"),
        FAIL("실패했습니다");

        String msg;
    }

    public <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    public <T> ListResult<T> getListResult(List<T> list) {
        ListResult<T> result = new ListResult<>();
        result.setData(list);
        setSuccessResult(result);
        return result;
    }

    public CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }

    public CommonResult getFailResult(String msg) {
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        result.setMsg(msg);
        return result;
    }

    private void setSuccessResult(CommonResult result) {
        result.setSuccess(true);
        result.setMsg(CommonResponse.SUCCESS.getMsg());
    }

    public <T> PageResult<T> getPageListResult(List<T> list, boolean hasPrev, boolean hasNext) {
        PageResult<T> result = new PageResult();
        result.setData(list);
        setSuccessResult(result);
        result.setHasPrev(hasPrev);
        result.setHasNext(hasNext);

        return result;
    }
}