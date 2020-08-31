package com.example.demo.service;

import com.example.demo.entity.SysUser;

/**
 * @author Jenkin
 * @date 2020/9/1 - 0:06
 */
public interface SysUserService {
  SysUser saveSysUser(SysUser sysUser);
  SysUser findSysUserByUsername(String username);
}
