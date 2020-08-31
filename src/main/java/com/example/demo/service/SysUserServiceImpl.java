package com.example.demo.service;

import com.example.demo.entity.SysUser;
import com.example.demo.repositoty.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Jenkin
 * @date 2020/9/1 - 0:06
 */
@Service
public class SysUserServiceImpl implements SysUserService{

  @Autowired
  private final SysUserRepository sysUserRepository;

  public SysUserServiceImpl(SysUserRepository sysUserRepository) {
    this.sysUserRepository = sysUserRepository;
  }

  @Override
  public SysUser saveSysUser(SysUser sysUser) {
    return sysUserRepository.save(sysUser);
  }

  @Override
  public SysUser findSysUserByUsername(String username) {
    SysUser sysUser = sysUserRepository.findByUsername(username);
    if (sysUser != null)return sysUser;
    throw new RuntimeException("User not exist");
  }
}
