package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Api(tags = {"Instrument"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/instr")
public class InstrumentController {

    private final ResponseService responseService;

    @ApiOperation(value = "악기 리스트 조회")
    @GetMapping("/")
    public ResponseEntity<ListResult<HashMap<String, String>>> getInstruments() {
        Instrument[] instrEn = Instrument.values();
        List<HashMap<String, String>> instrumentsList = new ArrayList<>();

        for (Instrument instrument : instrEn) {
            HashMap<String, String> instr = new HashMap<>();
            instr.put("id", instrument.getEn());
            instr.put("valueKr", instrument.getKo());
            instrumentsList.add(instr);
        }

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(instrumentsList));
    }
}
