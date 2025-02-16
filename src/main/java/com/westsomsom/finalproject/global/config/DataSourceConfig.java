package com.westsomsom.finalproject.global.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = "com.westsomsom.finalproject")
public class DataSourceConfig {

    // Write replica 정보로 만든 DataSource
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    // Read replica 정보로 만든 DataSource
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    // 읽기 모드인지 여부로 DataSource를 분기 처리
    @Bean
    @DependsOn({"writeDataSource", "readDataSource"})
    public DataSource routeDataSource(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource) {

        DataSourceRouter dataSourceRouter = new DataSourceRouter();
        HashMap<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("write", writeDataSource);
        dataSourceMap.put("read", readDataSource);
        dataSourceRouter.setTargetDataSources(dataSourceMap);
        dataSourceRouter.setDefaultTargetDataSource(writeDataSource);
        return dataSourceRouter;
    }

    @Bean
    @Primary
    @DependsOn({"routeDataSource"})
    public DataSource dataSource(@Qualifier("routeDataSource") DataSource routeDataSource) {
        return new LazyConnectionDataSourceProxy(routeDataSource);
    }
}
