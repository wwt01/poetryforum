package com.poetryforum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableElasticsearchRepositories(basePackages = "com.poetryforum.esdao") // 对应报错中的包名 esdao
@MapperScan("com.poetryforum.mapper")
@SpringBootApplication
public class PoetryforumApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoetryforumApplication.class, args);
    }

}
