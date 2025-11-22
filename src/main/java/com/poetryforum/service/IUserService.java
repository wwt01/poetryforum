package com.poetryforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poetryforum.dto.LoginFormDTO;
import com.poetryforum.dto.Result;
import com.poetryforum.entity.User;

import javax.servlet.http.HttpSession;


public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}
