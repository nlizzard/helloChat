package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nlizzard.api.feign.FileMicroServiceFeign;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.exceptions.GraceException;
import com.nlizzard.mapper.UsersMapper;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.bo.ModifyUserBO;
import com.nlizzard.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.nlizzard.grace.result.ResponseStatusEnum.USER_ISNOT_EXIST_ERROR;
import static com.nlizzard.grace.result.ResponseStatusEnum.WECHAT_NUM_ALREADY_MODIFIED_ERROR;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends BaseInfoProperties implements UsersService {

    private final UsersMapper usersMapper;

    private final FileMicroServiceFeign fileMicroServiceFeign;

    @Override
    public void modifyUserInfo(ModifyUserBO modifyUserBO) {
        Users pendingUser = new Users();
        String userId = modifyUserBO.getUserId();
        String wechatNum = modifyUserBO.getWechatNum();

        Users dbUser = usersMapper.selectById(userId);
        // 如果用户不存在
        if(dbUser == null) {
            GraceException.display(USER_ISNOT_EXIST_ERROR);
        }

        // 判断微信号是否进行修改：微信号需要限定唯一性和年度修改次数
        // 如果微信号不为空，并且用户已经修改过微信号了，则不能再修改了
        if(StringUtils.isNoneBlank(wechatNum) && redis.keyIsExist(REDIS_USER_ALREADY_UPDATE_WECHAT_NUM+":"+userId)){
            GraceException.display(WECHAT_NUM_ALREADY_MODIFIED_ERROR);
        }

        if(StringUtils.isNoneBlank(wechatNum)){
            // 设置新的微信二维码（getQrCodeUrl 内部 Feign 失败会返回 null）
            pendingUser.setWechatNumImg(getQrCodeUrl(wechatNum,userId));
        }

        pendingUser.setId(modifyUserBO.getUserId());
        pendingUser.setUpdatedTime(LocalDateTime.now());

        BeanUtils.copyProperties(modifyUserBO,pendingUser);
        // 更新用户信息
        usersMapper.updateById(pendingUser);

        if(StringUtils.isNoneBlank(wechatNum)){
            // 更新成功后再标记“已修改过微信号”，限制年度内只能改一次。
            // 若写在更新之前，一旦二维码生成或 DB 更新失败，用户会白白损失年度唯一修改机会。
            redis.setByDays(REDIS_USER_ALREADY_UPDATE_WECHAT_NUM+":"+userId
                            ,userId
                            ,365);
        }
    }

    @Override
    public Users getById(String userId) {
        return usersMapper.selectById(userId);
    }

    /**
     *  生成微信二维码，返回图片url
     */
    private String getQrCodeUrl(String wechatNumber, String userId) {
        try {
            return fileMicroServiceFeign.generatorQrCode(wechatNumber, userId);
        } catch (Exception e) {
            // throw new RuntimeException(e);
            return null;
        }
    }

    @Override
    public Users getByWechatNumOrMobile(String queryString) {
        LambdaQueryWrapper<Users> usersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        usersLambdaQueryWrapper.eq(Users::getWechatNum, queryString)
                .or()
                .eq(Users::getMobile, queryString);

        return usersMapper.selectOne(usersLambdaQueryWrapper);
    }
}
