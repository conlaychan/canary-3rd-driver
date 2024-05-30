package com.chenye.iot.canary.model;

public interface RegularExpression {

    /**
     * 设备编号，长度 4~32，接受以下字符：
     * 大小写字母
     * 数字
     * 下划线
     * 中划线
     * 点号
     * 特殊符号@
     */
    String DEVICE_ID = "^[\\w\\-.@]{4,32}$";
    String DEVICE_ID_ERROR = "仅支持大小写字母、数字、下划线、中划线、点号、特殊符号@，长度4~32";

    /**
     * 标识符 id 的正则表达式
     */
    String IDENTIFIER = "^\\w{1,50}$";
    String IDENTIFIER_ERROR = "仅支持大小写字母、数字、下划线，长度1~50";

    /**
     * 与标识符 id 配套的 name 的正则表达式
     */
    String NAME = "^[A-Za-z0-9\u4e00-\u9fa5][\u4e00-\u9fa5\\w\\-./]{0,29}$";
    String NAME_ERROR = "仅支持大小写字母、数字、下划线、中文、中横线、斜杠、点号，以字母、数字、中文开头，长度1~30";
}
