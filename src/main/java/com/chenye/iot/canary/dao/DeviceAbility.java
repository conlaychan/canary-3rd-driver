package com.chenye.iot.canary.dao;

import lombok.Data;

@Data
public class DeviceAbility {
    private Long id;
    private String deviceCode;
    private String abilityCode;
    private Long driverInstanceId;
    private String driverModelAbilityIdentifier;
    private String bindRule;
}
