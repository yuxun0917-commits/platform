package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 附件业务类型枚举
 *
 * <p>对应 sys_attachment 表的 biz_type 字段（tinyint）：标识该附件归属的业务场景，
 * 如用户头像、文章图片、文档附件等。前端上传时传入对应 code，后端据此归类与查询。</p>
 *
 * @author platform
 */
public enum AttachmentBizTypeEnum {

    /** 用户头像 */
    AVATAR(1, "头像"),
    /** 文章/富文本图片 */
    ARTICLE(2, "文章图片"),
    /** 文档附件 */
    DOCUMENT(3, "文档附件"),
    /** 导入模板 */
    IMPORT_TEMPLATE(4, "导入模板"),
    /** 其他 */
    OTHER(5, "其他");

    private final Integer code;
    private final String desc;

    AttachmentBizTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 附件类型码值
     * @return 枚举实例，code 不合法时返回 null
     */
    public static AttachmentBizTypeEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (AttachmentBizTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 附件类型码值
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(AttachmentBizTypeEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 附件类型码值
     * @return true 表示匹配
     */
    public boolean fromCode(Integer code) {
        return this.code.equals(code);
    }
}
