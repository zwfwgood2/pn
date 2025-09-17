//package com.example.provincialnode.controller;
//
//import com.example.provincialnode.common.Result;
//import com.example.provincialnode.service.SysAuthorizationLetterService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.util.Date;
//
///**
// * 授权书Controller
// * 处理授权书相关的HTTP请求
// */
//@Slf4j
//@RestController
//@RequestMapping("/api/authorization")
//public class AuthorizationController {
//
//    @Autowired
//    private SysAuthorizationLetterService authorizationLetterService;
//
//    /**
//     * 上传授权书
//     * @param file 授权书文件
//     * @param letterName 授权书名称
//     * @param authOrgId 授权机构ID
//     * @param authOrgName 授权机构名称
//     * @param startTime 授权开始时间
//     * @param endTime 授权结束时间
//     * @param description 描述
//     * @return 包含授权标识符的响应
//     */
//    @PostMapping("/upload")
//    public Result<String> uploadAuthorizationLetter(
//            @RequestParam("file") @NotNull(message = "授权书文件不能为空") MultipartFile file,
//            @RequestParam("letterName") @NotNull(message = "授权书名称不能为空") String letterName,
//            @RequestParam("authOrgId") @NotNull(message = "授权机构ID不能为空") Long authOrgId,
//            @RequestParam("authOrgName") @NotNull(message = "授权机构名称不能为空") String authOrgName,
//            @RequestParam("startTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//            @NotNull(message = "授权开始时间不能为空") Date startTime,
//            @RequestParam("endTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//            @NotNull(message = "授权结束时间不能为空") Date endTime,
//            @RequestParam(value = "description", required = false) String description) {
//
//        log.info("收到授权书上传请求，机构ID: {}, 授权书名称: {}", authOrgId, letterName);
//
//        // 调用服务层处理上传逻辑
//        String authIdentifier = authorizationLetterService.uploadAuthorizationLetter(
//                file, letterName, authOrgId, authOrgName, startTime, endTime, description);
//
//        log.info("授权书上传成功，授权标识符: {}", authIdentifier);
//        return Result.success(authIdentifier, "授权书上传成功");
//    }
//
//    /**
//     * 校验授权书
//     * @param authIdentifier 授权标识符
//     * @return 校验结果响应
//     */
//    @GetMapping("/validate")
//    public Result<Boolean> validateAuthorizationLetter(
//            @RequestParam("authIdentifier") @NotNull(message = "授权标识符不能为空") String authIdentifier) {
//
//        log.info("收到授权书校验请求，授权标识符: {}", authIdentifier);
//
//        // 调用服务层进行校验
//        boolean isValid = authorizationLetterService.validateAuthorizationLetter(authIdentifier);
//
//        if (isValid) {
//            log.info("授权书校验通过，授权标识符: {}", authIdentifier);
//            return Result.success(true, "授权书校验通过");
//        } else {
//            log.warn("授权书校验失败，授权标识符: {}", authIdentifier);
//            return Result.success(false, "授权书校验失败");
//        }
//    }
//
//    /**
//     * 获取授权书信息
//     * @param authIdentifier 授权标识符
//     * @return 授权书信息响应
//     */
//    @GetMapping("/info")
//    public Result<AuthorizationLetter> getAuthorizationLetterInfo(
//            @RequestParam("authIdentifier") @NotNull(message = "授权标识符不能为空") String authIdentifier) {
//
//        log.info("收到获取授权书信息请求，授权标识符: {}", authIdentifier);
//
//        // 调用服务层获取授权书信息
//        AuthorizationLetter letter = authorizationLetterService.getAuthorizationLetter(authIdentifier);
//
//        log.info("获取授权书信息成功，授权标识符: {}", authIdentifier);
//        return Result.success(letter, "获取授权书信息成功");
//    }
//
//    /**
//     * 查询机构的所有授权书
//     * @param authOrgId 授权机构ID
//     * @return 授权书列表响应
//     */
//    @GetMapping("/org/list")
//    public Result<Object> getLettersByOrgId(
//            @RequestParam("authOrgId") @NotNull(message = "授权机构ID不能为空") Long authOrgId) {
//
//        log.info("收到查询机构授权书请求，机构ID: {}", authOrgId);
//
//        // 调用服务层查询机构授权书
//        var letters = authorizationLetterService.getLettersByOrgId(authOrgId);
//
//        log.info("查询机构授权书成功，机构ID: {}, 数量: {}", authOrgId, letters.size());
//        return Result.success(letters, "查询成功");
//    }
//
//    /**
//     * 查询有效的授权书
//     * @return 有效授权书列表响应
//     */
//    @GetMapping("/valid/list")
//    public Result<Object> getValidLetters() {
//
//        log.info("收到查询有效授权书请求");
//
//        // 调用服务层查询有效授权书
//        var letters = authorizationLetterService.getValidLetters();
//
//        log.info("查询有效授权书成功，数量: {}", letters.size());
//        return Result.success(letters, "查询成功");
//    }
//
//    /**
//     * 更新授权书状态
//     * @param id 授权书ID
//     * @param status 状态（0:无效, 1:有效）
//     * @return 操作结果响应
//     */
//    @PostMapping("/status/update")
//    public Result<String> updateLetterStatus(
//            @RequestParam("id") @NotNull(message = "授权书ID不能为空") Long id,
//            @RequestParam("status") @NotNull(message = "状态不能为空") Integer status) {
//
//        log.info("收到更新授权书状态请求，ID: {}, 状态: {}", id, status);
//
//        // 调用服务层更新状态
//        authorizationLetterService.updateLetterStatus(id, status);
//
//        log.info("更新授权书状态成功，ID: {}, 状态: {}", id, status);
//        return Result.success(null, "更新成功");
//    }
//}