package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.response.SingleResult;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Api(tags = {"Instrument"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/instr")
public class InstrumentController {

    private final ResponseService responseService;

    @ApiOperation(value = "악기 리스트 조회")
    @GetMapping("/")
    public ResponseEntity<SingleResult<HashMap>> getInstruments() {
        Instrument[] instrEn = Instrument.values();
        List<String> instrKo = Stream.of(Instrument.values())
                .map(Instrument::getKo)
                .collect(Collectors.toList());
        HashMap<Instrument, String> instruments = new HashMap<>();

        for (int i = 0; i < instrEn.length; ++i) {
            instruments.put(instrEn[i], instrKo.get(i));
        }

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(instruments));
    }
}
