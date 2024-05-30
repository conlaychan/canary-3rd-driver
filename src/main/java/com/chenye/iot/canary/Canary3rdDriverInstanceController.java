package com.chenye.iot.canary;

import com.chenye.iot.canary.dao.DriverInstance;
import com.chenye.iot.canary.dao.DriverInstanceMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/driver-instance")
@Api(tags = "驱动实例")
public class Canary3rdDriverInstanceController {

    private final Canary3rdDriverInstanceScheduler scheduler;
    private final DriverInstanceMapper driverInstanceMapper;

    @ApiOperation("查询已启动的")
    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        return scheduler.list();
    }

    @ApiOperation("启动")
    @PostMapping("/start/{id}")
    public ResponseEntity<String> start(@PathVariable Long id) throws Exception {
        DriverInstance driverInstance = driverInstanceMapper.selectById(id);
        if (driverInstance == null) {
            return new ResponseEntity<>("数据库中没有此驱动实例", HttpStatus.NOT_FOUND);
        }
        if (scheduler.list().stream().anyMatch(map -> id.equals(map.get("id")))) {
            return new ResponseEntity<>("已经启动过了，无需重复启动", HttpStatus.OK);
        }
        scheduler.startDriverInstance(driverInstance);
        return new ResponseEntity<>("启动成功", HttpStatus.OK);
    }

    @ApiOperation("停止")
    @PostMapping("/stop/{id}")
    public ResponseEntity<String> stop(@PathVariable Long id) {
        scheduler.stop(id);
        return new ResponseEntity<>("已停止驱动实例", HttpStatus.OK);
    }
}
