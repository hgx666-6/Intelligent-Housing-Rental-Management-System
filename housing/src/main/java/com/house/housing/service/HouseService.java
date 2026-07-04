package com.house.housing.service;

import com.house.housing.dto.request.*;
import com.house.housing.dto.response.*;
import com.house.housing.vo.*;
import com.house.housing.common.Result;

import java.util.List;

/**
 * 房源服务接口
 */
public interface HouseService {

    // ========== 房东接口 ==========

    /**
     * 发布房源
     */
    Result<HouseResponseDTO> publishHouse(HouseRequestDTO request, Integer landlordId);

    /**
     * 编辑房源
     */
    Result<HouseResponseDTO> updateHouse(HouseRequestDTO request, Integer landlordId);

    /**
     * 获取我的房源列表（房东）
     */
    Result<PageResult<HouseResponseDTO>> getMyHouses(HouseQueryDTO query, Integer landlordId);

    /**
     * 下架房源
     */
    Result<Boolean> offlineHouse(Integer houseId, Integer landlordId);

    // ========== 租客/公共接口 ==========

    /**
     * 获取房源列表（租客浏览）
     */
    Result<PageResult<HouseResponseDTO>> getHouseList(HouseQueryDTO query);

    /**
     * 获取房源详情
     */
    Result<HouseResponseDTO> getHouseDetail(Integer houseId, Integer currentUserId);

    /**
     * 获取所有城市列表
     */
    Result<List<String>> getAllCities();

    /**
     * 获取所有房源类型
     */
    Result<List<String>> getAllHouseTypes();

    // ========== 管理员接口 ==========

    /**
     * 审核房源
     */
    Result<Boolean> approveHouse(Integer houseId, Integer status, String rejectReason);

    /**
     * 获取待审核房源列表（管理员）
     */
    Result<PageResult<HouseResponseDTO>> getPendingHouses(HouseQueryDTO query);

    /**
     * 统计待审核房源数量
     */
    Result<Long> countPending();
}