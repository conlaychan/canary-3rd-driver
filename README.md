第三方独立驱动，通过 redis 的 pub/sub 功能与平台通信。

# 驱动实例上报属性（点位）实时值到平台

平台订阅 redis 频道：up/device/property_value，数据结构示例：

```json
[
  {
    "driverInstanceId": 123,
    "propertyIdentifier": "brightness",
    "bindRule": "{\"lightId\": 111}",
    "value": "100",
    "readAt": "2023-01-12T11:22:33"
  },
  {
    "driverInstanceId": 123,
    "propertyIdentifier": "color",
    "bindRule": "{\"lightId\": 111}",
    "value": "1",
    "readAt": "2023-01-12T11:22:33"
  },
  {
    "driverInstanceId": 123,
    "propertyIdentifier": "switch",
    "bindRule": "{\"lightId\": 111}",
    "value": "true",
    "readAt": "2023-01-12T11:22:33"
  },
  {
    "driverInstanceId": 123,
    "propertyIdentifier": "personalName",
    "bindRule": "{\"lightId\": 111}",
    "value": "文本型属性",
    "readAt": "2023-01-12T11:22:33"
  },
  {
    "driverInstanceId": 123,
    "propertyIdentifier": "switchTime",
    "bindRule": "{\"lightId\": 111}",
    "value": "1673493753000",
    "readAt": "2023-01-12T11:22:33"
  },
  {
    "driverInstanceId": 123,
    "propertyIdentifier": "gps",
    "bindRule": "{\"lightId\": 111}",
    "value": "{\"coordinateSystem\":2,\"longitude\":3,\"latitude\":4}",
    "readAt": "2023-01-12T11:22:33"
  },
  {
    "driverInstanceId": 123,
    "propertyIdentifier": "testDay",
    "bindRule": "{\"lightId\": 111}",
    "value": "2023-08-14",
    "readAt": "2023-01-12T11:22:33"
  }
]
```

# 平台下发修改属性（点位）值的命令到驱动实例

驱动实例订阅 redis 频道：down/device/property_value，数据结构示例：

```json
{
  "driverInstanceId": 123,
  "jsonPropertyValueWrappers": [
    {
      "propertyIdentifier": "brightness",
      "bindRule": "{\"lightId\": 111}",
      "value": "100"
    },
    {
      "propertyIdentifier": "color",
      "bindRule": "{\"lightId\": 111}",
      "value": "1"
    },
    {
      "propertyIdentifier": "switch",
      "bindRule": "{\"lightId\": 111}",
      "value": "true"
    },
    {
      "propertyIdentifier": "personalName",
      "bindRule": "{\"lightId\": 111}",
      "value": "文本型属性"
    },
    {
      "propertyIdentifier": "switchTime",
      "bindRule": "{\"lightId\": 111}",
      "value": "1673493753000"
    },
    {
      "propertyIdentifier": "gps",
      "bindRule": "{\"lightId\": 111}",
      "value": "{\"coordinateSystem\":2,\"longitude\":3,\"latitude\":4}"
    },
    {
      "propertyIdentifier": "testDay",
      "bindRule": "{\"lightId\": 111}",
      "value": "2023-08-14"
    }
  ]
}
```

# 平台下发执行服务的命令到驱动实例

驱动实例订阅 redis 频道：down/device/service_intput，数据结构示例：

```json
{
  "driverInstanceId": 123,
  "serviceIdentifier": "testService",
  "serialNumber": 111,
  // 本次命令的唯一序号，仅执行同步服务时才有值，执行异步服务时没有值
  "inputData": {
    "decimalInput": "5.555",
    "enumInput": "1",
    "boolInput": "false",
    "textInput": "www",
    "dateInput": "1719417600000",
    "testDay": "2023-08-14",
    "gps": "{\"coordinateSystem\":2,\"longitude\":3,\"latitude\":4}",
    "configList": "[{\"switchTime\":1717171200000,\"switch\":false},{\"switchTime\":1718726400000,\"switch\":true}]"
  }
}
```

# 驱动实例上报执行同步服务的结果到平台

平台订阅 redis 频道：up/device/service_output，数据结构示例：

```json
{
  "driverInstanceId": 123,
  "abilityIdentifier": "testService",
  // 等于下发命令时的serviceIdentifier
  "readAt": "2023-01-12T11:22:33",
  // 执行完服务的时间
  "serialNumber": 111,
  // 等于下发命令时的serialNumber
  "outputData": {
    "decimalOutput": "5.555",
    "enumOutput": "1",
    "boolOutput": "false",
    "textOutput": "www",
    "dateOutput": "1719417600000",
    "testDay": "2023-08-14",
    "gps": "{\"coordinateSystem\":2,\"longitude\":3,\"latitude\":4}",
    "configList": "[{\"switchTime\":1717171200000,\"switch\":false},{\"switchTime\":1718726400000,\"switch\":true}]"
  }
}
```

# 驱动实例上报设备事件到平台

平台订阅 redis 频道：up/device/event，数据结构示例：

```json
{
  "driverInstanceId": 123,
  "abilityIdentifier": "lowBatteryAlert",
  // 事件编号（类型）
  "readAt": "2023-01-12T11:22:33",
  // 事件时间
  "outputData": {
    "decimalOutput": "5.555",
    "enumOutput": "1",
    "boolOutput": "false",
    "textOutput": "www",
    "dateOutput": "1719417600000",
    "testDay": "2023-08-14",
    "gps": "{\"coordinateSystem\":2,\"longitude\":3,\"latitude\":4}",
    "configList": "[{\"switchTime\":1717171200000,\"switch\":false},{\"switchTime\":1718726400000,\"switch\":true}]"
  }
}
```

# 开发说明

与 redis 的通信以及各种数据结构，都已经预先开发好了，代码在包 com.chenye.iot.canary 内，如无特殊情况通常不需要修改这个包内的代码。

第三方开发者开发的驱动代码请放在包 org.example.canary.driver 内，可以随意修改这个包的名称，包内预置的类 IotDriver
应始终与开发者开发的驱动在同一包内。

独立驱动的实例不能在平台的页面上控制启动和停止，请开发者自行实现。

从平台页面看到的独立驱动的实例的运行状态是不准确的，请开发者自行监视实例的运行状态。

包 org.example.canary.driver 内已经预置了一个模拟灯控系统的驱动
SmartLightMockDriver，请仔细审阅和理解，心领神会之后便可开始开发你自己的驱动啦~~~