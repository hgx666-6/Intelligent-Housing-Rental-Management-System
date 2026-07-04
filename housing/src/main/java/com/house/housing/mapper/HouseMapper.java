package com.house.housing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.house.housing.dto.request.*;
import com.house.housing.entity.House;
import com.house.housing.vo.CityHouseCountVO;
import com.house.housing.vo.HouseStatusCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 房源 Mapper 接口
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {

    /**
     * 多条件分页查询房源列表
     *
     * @param query 查询条件
     * @return 房源列表
     */
    List<House> selectHouseList(@Param("query") HouseQueryDTO query);

    /**
     * 统计符合条件的房源总数
     *
     * @param query 查询条件
     * @return 总数
     */
    Long countHouseList(@Param("query") HouseQueryDTO query);

    /**
     * 增加房源浏览次数
     *
     * @param houseId 房源ID
     * @return 影响行数
     */
    @Update("UPDATE houses SET view_count = view_count + 1 WHERE id = #{houseId}")
    int incrementViewCount(@Param("houseId") Integer houseId);

    /**
     * 获取待审核房源数量（管理员）
     *
     * @return 待审核数量
     */
    @Select("SELECT COUNT(*) FROM houses WHERE status = 0")
    Long countPending();

    /**
     * 获取所有城市列表（去重，仅已上架房源）
     *
     * @return 城市列表
     */
    @Select("SELECT DISTINCT city FROM houses WHERE status = 1 ORDER BY city")
    List<String> selectAllCities();

    /**
     * 获取所有房源类型列表（去重，仅已上架房源）
     *
     * @return 房源类型列表
     */
    @Select("SELECT DISTINCT house_type FROM houses WHERE status = 1 ORDER BY house_type")
    List<String> selectAllHouseTypes();

    /**
     * 根据城市统计房源数量
     *
     * @return 统计数据
     */
    @Select("SELECT city, COUNT(*) as count FROM houses WHERE status = 1 GROUP BY city ORDER BY count DESC")
    List<CityHouseCountVO> countHousesByCity();

    /**
     * 统计各状态的房源数量
     *
     * @return 统计数据
     */
    @Select("SELECT status, COUNT(*) as count FROM houses GROUP BY status")
    List<HouseStatusCountVO> countByStatus();

    /**
     * 获取房东的所有房源ID列表
     *
     * @param landlordId 房东ID
     * @return 房源ID列表
     */
    @Select("SELECT id FROM houses WHERE landlord_id = #{landlordId}")
    List<Integer> selectHouseIdsByLandlord(@Param("landlordId") Integer landlordId);

    /**
     * 更新房源状态
     *
     * @param houseId 房源ID
     * @param status  新状态
     * @return 影响行数
     */
    @Update("UPDATE houses SET status = #{status}, updated_at = NOW() WHERE id = #{houseId}")
    int updateStatus(@Param("houseId") Integer houseId, @Param("status") Integer status);

    /**
     * 批量更新房源状态
     *
     * @param houseIds 房源ID列表
     * @param status   新状态
     * @return 影响行数
     */
    @Update({
            "<script>",
            "UPDATE houses SET status = #{status}, updated_at = NOW()",
            "WHERE id IN",
            "<foreach collection='houseIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int batchUpdateStatus(@Param("houseIds") List<Integer> houseIds, @Param("status") Integer status);
}