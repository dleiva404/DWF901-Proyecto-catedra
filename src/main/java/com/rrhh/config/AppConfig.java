package com.rrhh.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.permisos.dao", "com.permisos.service"})
public class AppConfig {
}
