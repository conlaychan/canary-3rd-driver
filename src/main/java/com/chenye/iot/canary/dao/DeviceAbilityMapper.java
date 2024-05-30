package com.chenye.iot.canary.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceAbilityMapper extends BaseMapper<DeviceAbility> {

    List<DeviceAbility> listByDriverInstanceIdAndAbilityType(
            @Param("driverInstanceId") Long driverInstanceId,
            @Param("abilityType") String abilityType
    );

}
