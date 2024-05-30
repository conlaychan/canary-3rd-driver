package com.chenye.iot.canary.dao;

import lombok.Data;

@Data
public class DriverInstance {
    private Long id;
    private String driverName;
    private String driverInstanceName;
    private String initParam;
    private Boolean autoStartEnabled = true;
    private Integer cycleSeconds = 3;
    private Boolean standalone = true;
    private String runtimeLog;
}
