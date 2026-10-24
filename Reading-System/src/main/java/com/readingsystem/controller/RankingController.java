package com.readingsystem.controller;

import com.readingsystem.common.Result;
import com.readingsystem.service.RankingService;
import com.readingsystem.vo.RankingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 排行榜
 */
@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public Result<List<RankingVO>> rank(@RequestParam(defaultValue = "1") Integer rankType,
                                        @RequestParam(defaultValue = "1") Integer scopeType) {
        return Result.ok(rankingService.rank(rankType, scopeType));
    }
}
