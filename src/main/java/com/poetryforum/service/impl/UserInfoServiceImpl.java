package com.poetryforum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poetryforum.mapper.UserInfoMapper;
import com.poetryforum.service.IUserInfoService;
import org.springframework.stereotype.Service;
import com.poetryforum.entity.UserInfo;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
