package com.huangbo.oracle.export.entity;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.entity
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class Column {
    /**
     * 列名
     */
    private String name;
    /**
     * 列类型
     */
    private String type;
    /**
     * 列注释
     */
    private String comment;
    /**
     * 列位置
     */
    private int position;
    /**
     * 列限制是否可以为空
     */
    private Boolean nullable = true;

    public Column() {

    }

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }
}
