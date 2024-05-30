package com.chenye.iot.canary.model

data class ThingModel constructor(
    val properties: List<ThingModelProperty>,
    val services: List<ThingModelService>,
    val events: List<ThingModelEvent>
) {
    init {
        val validateSchema = this.validateSchema()
        if (validateSchema.failed) {
            throw IllegalArgumentException(validateSchema.failError())
        }
    }

    /**
     * 校验自身的全部数据和结构的正确性，包括：id、name的唯一性，出入参的数据结构正确性，属性的数据结构正确性，id、name符合正则表达式
     */
    fun validateSchema(): Result {
        val validateUnique = validateUnique(this)
        if (validateUnique.failed) {
            return validateUnique
        }
        val validateIOData = validateIOData(this)
        if (validateIOData.failed) {
            return validateIOData
        }
        val validatePropertySchema = validatePropertySchema(properties)
        if (validatePropertySchema.failed) {
            return validatePropertySchema
        }
        val regexIdName = regexIdName(this)
        if (regexIdName.failed) {
            return regexIdName
        }
        return Result.succeed()
    }

    companion object {
        /**
         * 正则校验 identifier
         */
        fun regexIdentifier(identifier: String, errorPrefix: String): Result {
            if (!Patterns.IDENTIFIER.matches(identifier)) {
                return Result.fail("${errorPrefix}id【$identifier】不符合规范：${RegularExpression.IDENTIFIER_ERROR}")
            }
            return Result.succeed()
        }

        /**
         * 正则校验名称
         */
        fun regexName(name: String, errorPrefix: String): Result {
            if (!Patterns.NAME.matches(name)) {
                return Result.fail("${errorPrefix}名称【$name】不符合规范：${RegularExpression.NAME_ERROR}")
            }
            return Result.succeed()
        }

        /**
         * 正则校验 id 和 name
         */
        private fun regexIdName(thingModel: ThingModel): Result {
            for (property in thingModel.properties) {
                val result = regexIdentifier(property.identifier, "属性")
                if (result.failed) {
                    return result
                }
                val regexName = regexName(property.name, "属性")
                if (regexName.failed) {
                    return regexName
                }
            }
            for (service in thingModel.services) {
                val result = regexIdentifier(service.identifier, "服务")
                if (result.failed) {
                    return result
                }
                val regexName = regexName(service.name, "服务")
                if (regexName.failed) {
                    return regexName
                }
            }
            for (event in thingModel.events) {
                val result = regexIdentifier(event.identifier, "事件")
                if (result.failed) {
                    return result
                }
                val regexName = regexName(event.name, "事件")
                if (regexName.failed) {
                    return regexName
                }
            }
            return Result.succeed()
        }

        /**
         * 校验出入参
         */
        private fun validateIOData(thingModel: ThingModel): Result {
            for (service in thingModel.services) {
                val identifier = service.identifier
                val name = service.name
                val validateInputData = validateIOData(service.inputData, "服务【$identifier/$name】的入参")
                if (validateInputData.failed) {
                    return validateInputData
                }
                val validateOutputData = validateIOData(service.outputData, "服务【$identifier/$name】的出参")
                if (validateOutputData.failed) {
                    return validateOutputData
                }
            }
            for (event in thingModel.events) {
                val identifier = event.identifier
                val name = event.name
                val validateOutputData = validateIOData(event.outputData, "事件【$identifier/$name】的出参")
                if (validateOutputData.failed) {
                    return validateOutputData
                }
            }
            return Result.succeed()
        }

        private fun validateIOData(ioDatas: List<IOData>, errorPrefix: String): Result {
            val ids = mutableSetOf<String>()
            val names = mutableSetOf<String>()
            for (ioData in ioDatas) {
                val identifier = ioData.identifier
                val name = ioData.name
                if (ids.contains(identifier)) {
                    return Result.fail("${errorPrefix}id【$identifier】重复")
                } else {
                    ids.add(identifier)
                }
                if (names.contains(name)) {
                    return Result.fail("${errorPrefix}名称【$name】重复")
                } else {
                    names.add(name)
                }
                val validateSchema = ioData.dataType.specs.validateSchema()
                if (validateSchema.failed) {
                    return Result.fail("${errorPrefix}【$identifier/$name】定义错误：" + validateSchema.failError())
                }
                val regexIdentifier = regexIdentifier(identifier, errorPrefix)
                if (regexIdentifier.failed) {
                    return regexIdentifier
                }
                val regexName = regexName(name, errorPrefix)
                if (regexName.failed) {
                    return regexName
                }
            }
            return Result.succeed()
        }

        /**
         * 校验 id 和 name 的唯一性
         */
        private fun validateUnique(thingModel: ThingModel): Result {
            val ids = mutableSetOf<String>()
            val names = mutableSetOf<String>()
            for (property in thingModel.properties) {
                val identifier = property.identifier
                val name = property.name
                if (ids.contains(identifier)) {
                    return Result.fail("能力id【$identifier】重复")
                } else {
                    ids.add(identifier)
                }
                if (names.contains(name)) {
                    return Result.fail("能力名称【$name】重复")
                } else {
                    names.add(name)
                }
            }
            for (service in thingModel.services) {
                val identifier = service.identifier
                val name = service.name
                if (ids.contains(identifier)) {
                    return Result.fail("能力id【$identifier】重复")
                } else {
                    ids.add(identifier)
                }
                if (names.contains(name)) {
                    return Result.fail("能力名称【$name】重复")
                } else {
                    names.add(name)
                }
            }
            for (event in thingModel.events) {
                val identifier = event.identifier
                val name = event.name
                if (ids.contains(identifier)) {
                    return Result.fail("能力id【$identifier】重复")
                } else {
                    ids.add(identifier)
                }
                if (names.contains(name)) {
                    return Result.fail("能力名称【$name】重复")
                } else {
                    names.add(name)
                }
            }
            return Result.succeed()
        }

        /**
         * 校验属性的数据定义
         */
        private fun validatePropertySchema(properties: List<ThingModelProperty>): Result {
            for (property in properties) {
                val validateSchema = property.dataType.specs.validateSchema()
                if (validateSchema.failed) {
                    return Result.fail("属性【${property.identifier}/${property.name}】定义错误：" + validateSchema.failError())
                }
            }
            return Result.succeed()
        }
    }
}
