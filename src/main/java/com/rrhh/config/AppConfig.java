package com.rrhh.config;

import com.permisos.util.JPAUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;

@Configuration
@ComponentScan(basePackages = {"com.permisos.dao", "com.permisos.service", "com.rrhh.api.security"})
@EnableJpaRepositories(basePackages = "com.rrhh.repository")
@EnableTransactionManagement
public class AppConfig {

    // Se reutiliza el EntityManagerFactory de JPAUtil: una sola configuración de BD.
    @Bean(destroyMethod = "")
    public EntityManagerFactory entityManagerFactory() {
        return JPAUtil.getEntityManagerFactory();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
