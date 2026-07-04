package com.house.housing.service.impl;

import com.house.housing.dto.request.*;
import com.house.housing.dto.response.*;
import com.house.housing.vo.*;
import com.house.housing.common.Result;
import com.house.housing.entity.House;
import com.house.housing.entity.User;
import com.house.housing.enums.*;
import com.house.housing.mapper.*;
import com.house.housing.service.HouseService;
import com.house.housing.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.alibaba.fastjson.JSON;

@Service
@Slf4j
public class HouseServiceImpl implements HouseService {

    private final HouseMapper houseMapper;
    private final UserMapper userMapper;

    public HouseServiceImpl(HouseMapper houseMapper, UserMapper userMapper) {
        this.houseMapper = houseMapper;
        this.userMapper = userMapper;
    }

    // ========== 房东接口 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<HouseResponseDTO> publishHouse(HouseRequestDTO request, Integer landlordId) {
        // 检查房东是否存在
        User landlord = userMapper.selectById(landlordId);
        if (landlord == null) {
            return Result.error("房东不存在");
        }
        if (!landlord.getRole().equals(UserRoleEnum.LANDLORD.getCode())) {
            return Result.error("用户不是房东，无法发布房源");
        }

        // 构建实体
        House house = new House();
        BeanUtils.copyProperties(request, house);
        house.setLandlordId(landlordId);
        house.setStatus(HouseStatusEnum.PENDING.getCode());
        house.setViewCount(0);

        // 处理JSON字段
        if (request.getFacilities() != null && !request.getFacilities().isEmpty()) {
            house.setFacilities(JSON.toJSONString(request.getFacilities()));
        }
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            house.setImages(JSON.toJSONString(request.getImages()));
        }

        // 保存
        int result = houseMapper.insert(house);
        if (result <= 0) {
            return Result.error("发布房源失败");
        }

        // 返回详情
        return getHouseDetail(house.getId(), landlordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<HouseResponseDTO> updateHouse(HouseRequestDTO request, Integer landlordId) {
        if (request.getId() == null) {
            return Result.error("房源ID不能为空");
        }

        // 检查房源是否存在且属于该房东
        House oldHouse = houseMapper.selectById(request.getId());
        if (oldHouse == null) {
            return Result.error("房源不存在");
        }
        if (!oldHouse.getLandlordId().equals(landlordId)) {
            return Result.error("无权编辑此房源");
        }
        if (oldHouse.getStatus().equals(HouseStatusEnum.RENTED.getCode())) {
            return Result.error("已租出房源不可编辑");
        }

        // 更新
        House house = new House();
        BeanUtils.copyProperties(request, house);

        // 处理JSON字段
        if (request.getFacilities() != null) {
            house.setFacilities(JSON.toJSONString(request.getFacilities()));
        }
        if (request.getImages() != null) {
            house.setImages(JSON.toJSONString(request.getImages()));
        }

        // 编辑后变为待审核状态
        house.setStatus(HouseStatusEnum.PENDING.getCode());
        house.setUpdatedAt(LocalDateTime.now());

        int result = houseMapper.updateById(house);
        if (result <= 0) {
            return Result.error("编辑房源失败");
        }

        return getHouseDetail(house.getId(), landlordId);
    }

    @Override
    public Result<PageResult<HouseResponseDTO>> getMyHouses(HouseQueryDTO query, Integer landlordId) {
        // 验证房东身份
        User landlord = userMapper.selectById(landlordId);
        if (landlord == null || !landlord.getRole().equals(UserRoleEnum.LANDLORD.getCode())) {
            return Result.error("用户不是房东");
        }

        query.setLandlordId(landlordId);
        List<House> houses = houseMapper.selectHouseList(query);
        Long total = houseMapper.countHouseList(query);

        List<HouseResponseDTO> records = houses.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        PageResult<HouseResponseDTO> pageResult = new PageResult<>(records, total, query.getPage(), query.getLimit());
        return Result.success(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> offlineHouse(Integer houseId, Integer landlordId) {
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            return Result.error("房源不存在");
        }
        if (!house.getLandlordId().equals(landlordId)) {
            return Result.error("无权操作此房源");
        }
        if (house.getStatus().equals(HouseStatusEnum.RENTED.getCode())) {
            return Result.error("已租出房源不可下架，请先解除合同");
        }
        if (house.getStatus().equals(HouseStatusEnum.OFFLINE.getCode())) {
            return Result.error("房源已下架，请勿重复操作");
        }

        house.setStatus(HouseStatusEnum.OFFLINE.getCode());
        house.setUpdatedAt(LocalDateTime.now());
        int result = houseMapper.updateById(house);

        if (result <= 0) {
            return Result.error("下架失败");
        }
        return Result.success("下架成功", true);
    }

    // ========== 租客/公共接口 ==========

    @Override
    public Result<PageResult<HouseResponseDTO>> getHouseList(HouseQueryDTO query) {
        // 默认只查已上架
        if (query.getStatus() == null && query.getLandlordId() == null) {
            query.setStatus(HouseStatusEnum.ONLINE.getCode());
        }

        List<House> houses = houseMapper.selectHouseList(query);
        Long total = houseMapper.countHouseList(query);

        List<HouseResponseDTO> records = houses.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        PageResult<HouseResponseDTO> pageResult = new PageResult<>(records, total, query.getPage(), query.getLimit());
        return Result.success(pageResult);
    }

    @Override
    public Result<HouseResponseDTO> getHouseDetail(Integer houseId, Integer currentUserId) {
        // 先查房源
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            return Result.error("房源不存在");
        }

        // 非管理员且未登录用户查看已下架/待审核房源
        if (currentUserId == null && !house.getStatus().equals(HouseStatusEnum.ONLINE.getCode())) {
            return Result.error("房源不可查看");
        }

        // 普通用户查看非自己发布的非上架房源
        if (currentUserId != null && !house.getLandlordId().equals(currentUserId)) {
            User user = userMapper.selectById(currentUserId);
            if (user == null || !user.getRole().equals(UserRoleEnum.ADMIN.getCode())) {
                if (!house.getStatus().equals(HouseStatusEnum.ONLINE.getCode())) {
                    return Result.error("房源不可查看");
                }
            }
        }

        // 增加浏览次数（仅租客浏览时增加）
        if (currentUserId != null) {
            User user = userMapper.selectById(currentUserId);
            if (user != null && user.getRole().equals(UserRoleEnum.TENANT.getCode())) {
                houseMapper.incrementViewCount(houseId);
            }
        }

        // 查询房东信息
        User landlord = userMapper.selectById(house.getLandlordId());
        HouseResponseDTO dto = convertToDetailResponseDTO(house, landlord);

        return Result.success(dto);
    }

    @Override
    public Result<List<String>> getAllCities() {
        List<String> cities = houseMapper.selectAllCities();
        return Result.success(cities);
    }

    @Override
    public Result<List<String>> getAllHouseTypes() {
        List<String> types = houseMapper.selectAllHouseTypes();
        return Result.success(types);
    }

    // ========== 管理员接口 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> approveHouse(Integer houseId, Integer status, String rejectReason) {
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            return Result.error("房源不存在");
        }
        if (!house.getStatus().equals(HouseStatusEnum.PENDING.getCode())) {
            return Result.error("该房源不在待审核状态");
        }

        // 1-通过 2-驳回
        if (status == 1) {
            house.setStatus(HouseStatusEnum.ONLINE.getCode());
        } else if (status == 2) {
            house.setStatus(HouseStatusEnum.OFFLINE.getCode());
            // 可记录驳回原因到日志
            log.info("房源 {} 审核驳回，原因：{}", houseId, rejectReason);
        } else {
            return Result.error("审核状态不正确，1-通过，2-驳回");
        }

        house.setUpdatedAt(LocalDateTime.now());
        int result = houseMapper.updateById(house);

        if (result <= 0) {
            return Result.error("审核失败");
        }
        return Result.success("审核完成", true);
    }

    @Override
    public Result<PageResult<HouseResponseDTO>> getPendingHouses(HouseQueryDTO query) {
        query.setStatus(HouseStatusEnum.PENDING.getCode());
        List<House> houses = houseMapper.selectHouseList(query);
        Long total = houseMapper.countHouseList(query);

        List<HouseResponseDTO> records = houses.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        PageResult<HouseResponseDTO> pageResult = new PageResult<>(records, total, query.getPage(), query.getLimit());
        return Result.success(pageResult);
    }

    @Override
    public Result<Long> countPending() {
        Long count = houseMapper.countPending();
        return Result.success(count);
    }

    // ========== 私有转换方法 ==========

    /**
     * 将 House 实体转换为 HouseResponseDTO（列表用）
     */
    private HouseResponseDTO convertToResponseDTO(House house) {
        HouseResponseDTO dto = new HouseResponseDTO();
        BeanUtils.copyProperties(house, dto);
        dto.setStatusDesc(HouseStatusEnum.getDescByCode(house.getStatus()));
        dto.setFacilities(parseJsonList(house.getFacilities()));
        dto.setImages(parseJsonList(house.getImages()));
        return dto;
    }

    /**
     * 将 House 实体转换为 HouseResponseDTO（详情用，包含房东信息）
     */
    private HouseResponseDTO convertToDetailResponseDTO(House house, User landlord) {
        HouseResponseDTO dto = convertToResponseDTO(house);
        if (landlord != null) {
            dto.setLandlordName(landlord.getRealName());
            dto.setLandlordPhone(landlord.getPhone());
        }
        return dto;
    }

    /**
     * 解析 JSON 字符串为 List
     */
    private List<String> parseJsonList(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return JSON.parseArray(json, String.class);
        } catch (Exception e) {
            log.warn("解析JSON失败: {}", json);
            return new ArrayList<>();
        }
    }
}