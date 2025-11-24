package com.poetryforum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poetryforum.entity.BlogComments;
import com.poetryforum.mapper.BlogCommentsMapper;
import com.poetryforum.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
