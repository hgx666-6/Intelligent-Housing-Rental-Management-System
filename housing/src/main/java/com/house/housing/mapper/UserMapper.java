package com.house.housing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.house.housing.dto.request.UserQueryDTO;
import com.house.housing.entity.User;
import com.house.housing.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 分页查询用户列表（管理员）
     */
    List<User> selectUserList(@Param("query") UserQueryDTO query);

    /**
     * 统计用户总数
     */
    Long countUserList(@Param("query") UserQueryDTO query);

    /**
     * 更新用户状态
     */
    @Update("UPDATE users SET status = #{status} WHERE id = #{userId}")
    int updateStatus(@Param("userId") Integer userId, @Param("status") Integer status);

    /**
     * 统计各角色用户数量
     */
    @Select("SELECT role, COUNT(*) as count FROM users GROUP BY role")
    List<RoleCountVO> countByRole();

    /**
     * 统计今日新增用户
     */
    @Select("SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()")
    int countTodayNewUsers();
}