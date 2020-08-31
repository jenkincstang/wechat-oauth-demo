package com.example.demo.repositoty;

import com.example.demo.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Jenkin
 * @date 2020/9/1 - 0:04
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser,Integer> {
  SysUser findByUsername(String username);
}
