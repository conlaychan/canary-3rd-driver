package com.chenye.iot.canary.model

import java.math.BigDecimal
import java.math.BigInteger

object Demo {
    val THING_MODEL = ThingModel(
        listOf(
            ThingModelProperty(
                "property_bigint", "属性-bigint", AccessMode.READ_WRITE,
                DataSpecsInt(BigInteger.ONE, BigInteger.valueOf(100), BigInteger.ONE, "s", "秒").dataTypeSpecs()
            ),
            ThingModelProperty(
                "property_decimal", "属性-decimal", AccessMode.READ_WRITE,
                DataSpecsDecimal(BigDecimal("0.01"), BigDecimal.TEN, BigDecimal("0.01")).dataTypeSpecs()
            ),
            ThingModelProperty(
                "property_enum", "属性-enum", AccessMode.READ_WRITE,
                DataSpecsEnum(1 to "111", 2 to "222", 3 to "333").dataTypeSpecs()
            ),
            ThingModelProperty(
                "property_bool", "属性-bool", AccessMode.READ_WRITE,
                DataSpecsBool("000", "111").dataTypeSpecs()
            ),
            ThingModelProperty(
                "property_text", "属性-text", AccessMode.READ_WRITE,
                DataSpecsText(666).dataTypeSpecs()
            ),
            ThingModelProperty(
                "property_date", "属性-date", AccessMode.READ_WRITE,
                DataSpecsDate().dataTypeSpecs()
            ),

            ThingModelProperty(
                "property_struct", "属性-struct", AccessMode.READ_WRITE,
                DataSpecsStruct(
                    ThingModelStructProperty(
                        "struct_bigint",
                        "结构体内嵌属性-bigint",
                        DataSpecsInt(BigInteger.ONE, BigInteger.valueOf(123), BigInteger.ONE).dataTypeSpecsStruct()
                    ),
                    ThingModelStructProperty(
                        "struct_enum",
                        "结构体内嵌属性-enum",
                        DataSpecsEnum(1 to "111", 2 to "222", 3 to "333").dataTypeSpecsStruct()
                    ),
                    ThingModelStructProperty(
                        "struct_bool",
                        "结构体内嵌属性-bool",
                        DataSpecsBool("000", "111").dataTypeSpecsStruct()
                    ),
                    ThingModelStructProperty("struct_text", "结构体内嵌属性-text", DataSpecsText(null).dataTypeSpecsStruct()),
                    ThingModelStructProperty("struct_date", "结构体内嵌属性-date", DataSpecsDate().dataTypeSpecsStruct())
                ).dataTypeSpecs()
            ),

            ThingModelProperty(
                "property_array_double", "数组属性内嵌了数字", AccessMode.READ_WRITE,
                DataSpecsArray(10, DataSpecsDecimal(BigDecimal("0.01"), BigDecimal.TEN, BigDecimal("0.01")).dataTypeSpecsArray()).dataTypeSpecs()
            ),

            ThingModelProperty(
                "property_array_struct", "数组属性内嵌了结构体", AccessMode.READ_WRITE,
                DataSpecsArray(
                    10,
                    DataSpecsStruct(
                        ThingModelStructProperty(
                            "struct_bigint",
                            "结构体内嵌属性-bigint",
                            DataSpecsInt(
                                BigInteger.ONE,
                                BigInteger.valueOf(123),
                                BigInteger.ONE
                            ).dataTypeSpecsStruct()
                        ),
                        ThingModelStructProperty(
                            "struct_enum",
                            "结构体内嵌属性-enum",
                            DataSpecsEnum(1 to "111", 2 to "222", 3 to "333").dataTypeSpecsStruct()
                        ),
                        ThingModelStructProperty(
                            "struct_bool",
                            "结构体内嵌属性-bool",
                            DataSpecsBool("000", "111").dataTypeSpecsStruct()
                        ),
                        ThingModelStructProperty(
                            "struct_text",
                            "结构体内嵌属性-text",
                            DataSpecsText(null).dataTypeSpecsStruct()
                        ),
                        ThingModelStructProperty(
                            "struct_date",
                            "结构体内嵌属性-date",
                            DataSpecsDate().dataTypeSpecsStruct()
                        )
                    ).dataTypeSpecsArray()
                ).dataTypeSpecs()
            )
        ),
        listOf(
            ThingModelService(
                "service_1", "服务名称", CallType.SYNC,
                listOf(
                    IOData(
                        "input_param_1", "入参1",
                        DataSpecsArray(
                            10,
                            DataSpecsStruct(
                                ThingModelStructProperty(
                                    "struct_bigint",
                                    "结构体内嵌属性-bigint",
                                    DataSpecsInt(
                                        BigInteger.ONE,
                                        BigInteger.valueOf(123),
                                        BigInteger.ONE
                                    ).dataTypeSpecsStruct()
                                ),
                                ThingModelStructProperty(
                                    "struct_enum",
                                    "结构体内嵌属性-enum",
                                    DataSpecsEnum(1 to "111", 2 to "222", 3 to "333").dataTypeSpecsStruct()
                                ),
                                ThingModelStructProperty(
                                    "struct_bool",
                                    "结构体内嵌属性-bool",
                                    DataSpecsBool("000", "111").dataTypeSpecsStruct()
                                ),
                                ThingModelStructProperty(
                                    "struct_text",
                                    "结构体内嵌属性-text",
                                    DataSpecsText(null).dataTypeSpecsStruct()
                                ),
                                ThingModelStructProperty(
                                    "struct_date",
                                    "结构体内嵌属性-date",
                                    DataSpecsDate().dataTypeSpecsStruct()
                                )
                            ).dataTypeSpecsArray()
                        ).dataTypeSpecs()
                    )
                ),
                listOf(
                    IOData(
                        "output_param_1", "出参1",
                        DataSpecsInt(
                            BigInteger.ONE,
                            BigInteger.valueOf(100),
                            BigInteger.ONE,
                            "s",
                            "秒"
                        ).dataTypeSpecs()
                    )
                )
            )
        ),
        listOf(
            ThingModelEvent(
                "event_a", "事件名称", EventType.ALERT,
                listOf(
                    IOData(
                        "output_id", "事件出参名称",
                        DataSpecsStruct(
                            ThingModelStructProperty(
                                "Longitude",
                                "经度",
                                DataSpecsDecimal(
                                    BigDecimal("-180"),
                                    BigDecimal("180"),
                                    BigDecimal("0.0001"),
                                    "°",
                                    "度"
                                ).dataTypeSpecsStruct()
                            ),
                            ThingModelStructProperty(
                                "Latitude",
                                "纬度",
                                DataSpecsDecimal(
                                    BigDecimal("-90"),
                                    BigDecimal("90"),
                                    BigDecimal("0.0001"),
                                    "°",
                                    "度"
                                ).dataTypeSpecsStruct()
                            ),
                            ThingModelStructProperty(
                                "Altitude",
                                "海拔",
                                DataSpecsDecimal(
                                    BigDecimal("-999999999"),
                                    BigDecimal("999999999"),
                                    BigDecimal("0.01"),
                                    "m",
                                    "米"
                                ).dataTypeSpecsStruct()
                            ),
                            ThingModelStructProperty(
                                "CoordinateSystem",
                                "坐标系统",
                                DataSpecsEnum(1 to "WGS_84", 2 to "GCJ_02").dataTypeSpecsStruct()
                            )
                        ).dataTypeSpecs()
                    )
                )
            )
        )
    )

}
