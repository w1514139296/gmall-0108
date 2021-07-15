package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

@Data
public class GroupVo {
    private String groupName;
    private List<AttrValueVo> attrs;
}
