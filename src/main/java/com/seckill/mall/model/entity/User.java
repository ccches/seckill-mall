package com.seckill.mall.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;          // BCrypt 密文
    private String nickname;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer status;           // 1=正常, 0=禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
