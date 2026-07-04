package com.house.housing.controller;

import com.house.housing.dto.request.*;
import com.house.housing.dto.response.*;
import com.house.housing.vo.*;
import com.house.housing.common.Result;
import com.house.housing.service.HouseService;
import com.house.housing.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/houses")
@Validated
public class HouseController {

    @Autowired
    private HouseService houseService;

    // ========== 房东接口 ==========

    @PostMapping
    public Result<HouseResponseDTO> publishHouse(
            @Valid @RequestBody HouseRequestDTO request,
            HttpServletRequest req) {
        Integer landlordId = getCurrentUserId(req);
        return houseService.publishHouse(request, landlordId);
    }

    @PutMapping("/{houseId}")
    public Result<HouseResponseDTO> updateHouse(
            @PathVariable Integer houseId,
            @Valid @RequestBody HouseRequestDTO request,
            HttpServletRequest req) {
        Integer landlordId = getCurrentUserId(req);
        request.setId(houseId);
        return houseService.updateHouse(request, landlordId);
    }

    @GetMapping("/my")
    public Result<PageResult<HouseResponseDTO>> getMyHouses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer status,
            HttpServletRequest req) {
        Integer landlordId = getCurrentUserId(req);
        HouseQueryDTO query = HouseQueryDTO.builder()
                .page(page)
                .limit(limit)
                .status(status)
                .build();
        return houseService.getMyHouses(query, landlordId);
    }

    @PutMapping("/{houseId}/offline")
    public Result<Boolean> offlineHouse(
            @PathVariable Integer houseId,
            HttpServletRequest req) {
        Integer landlordId = getCurrentUserId(req);
        return houseService.offlineHouse(houseId, landlordId);
    }

    // ========== 租客/公共接口 ==========

    @GetMapping
    public Result<PageResult<HouseResponseDTO>> getHouseList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) String keyword) {
        HouseQueryDTO query = HouseQueryDTO.builder()
                .page(page)
                .limit(limit)
                .city(city)
                .district(district)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .houseType(houseType)
                .keyword(keyword)
                .build();
        return houseService.getHouseList(query);
    }

    @GetMapping("/{houseId}")
    public Result<HouseResponseDTO> getHouseDetail(
            @PathVariable Integer houseId,
            HttpServletRequest req) {
        Integer currentUserId = getCurrentUserId(req);
        return houseService.getHouseDetail(houseId, currentUserId);
    }

    @GetMapping("/cities")
    public Result<List<String>> getAllCities() {
        return houseService.getAllCities();
    }

    @GetMapping("/types")
    public Result<List<String>> getAllHouseTypes() {
        return houseService.getAllHouseTypes();
    }

    // ========== 管理员接口 ==========
    @PutMapping("/{houseId}/approve")
    public Result<Boolean> approveHouse(
            @PathVariable Integer houseId,
            @RequestParam Integer status,
            @RequestParam(required = false) String rejectReason) {
        return houseService.approveHouse(houseId, status, rejectReason);
    }

    @GetMapping("/pending")
    public Result<PageResult<HouseResponseDTO>> getPendingHouses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        HouseQueryDTO query = HouseQueryDTO.builder()
                .page(page)
                .limit(limit)
                .build();
        return houseService.getPendingHouses(query);
    }

    @GetMapping("/pending/count")
    public Result<Long> countPending() {
        return houseService.countPending();
    }

    // ========== 辅助方法 ==========

    private Integer getCurrentUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }
}