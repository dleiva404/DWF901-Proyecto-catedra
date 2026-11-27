package com.rrhh.config;

import com.rrhh.api.ApiConfig;
import com.rrhh.api.security.ApiAccessDeniedHandler;
import com.rrhh.api.security.ApiAuthenticationEntryPoint;
import com.rrhh.api.security.BearerTokenFilter;
import com.rrhh.api.security.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Seguridad de la API REST (/api/**): sin sesiones, con token Bearer.
 * La web JSF y los servlets no pasan por esta cadena.
 *
 * Matriz de roles: lo que no esté listado aquí se deniega.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String EMPLEADO = "EMPLEADO";
    private static final String JEFATURA = "JEFATURA";
    private static final String RRHH = "RRHH";

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http, TokenService tokenService)
            throws Exception {

        http.antMatcher("/api/**");

        http.csrf().disable();
        http.formLogin().disable();
        http.httpBasic().disable();
        http.logout().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors().configurationSource(corsConfigurationSource());

        http.exceptionHandling()
                .authenticationEntryPoint(new ApiAuthenticationEntryPoint())
                .accessDeniedHandler(new ApiAccessDeniedHandler());

        http.addFilterBefore(new BearerTokenFilter(tokenService),
                UsernamePasswordAuthenticationFilter.class);

        http.authorizeRequests()
                // públicos
                .antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/ping").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/recuperar").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/restablecer").permitAll()
                // cualquier usuario autenticado
                .antMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                .antMatchers(HttpMethod.POST, "/api/auth/cambiar-contrasena").authenticated()
                // catálogo y saldo
                .antMatchers(HttpMethod.GET, "/api/tipos-solicitud").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/vacaciones/saldo").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                // solicitudes
                .antMatchers(HttpMethod.POST, "/api/solicitudes/*/aprobar").hasRole(JEFATURA)
                .antMatchers(HttpMethod.POST, "/api/solicitudes/*/rechazar").hasRole(JEFATURA)
                .antMatchers(HttpMethod.POST, "/api/solicitudes").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/solicitudes").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/solicitudes/*").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/solicitudes/*/historial").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.DELETE, "/api/solicitudes/*").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                // constancias
                .antMatchers(HttpMethod.POST, "/api/constancias/*/aprobar").hasRole(RRHH)
                .antMatchers(HttpMethod.POST, "/api/constancias/*/rechazar").hasRole(RRHH)
                .antMatchers(HttpMethod.POST, "/api/constancias").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/constancias").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/constancias/*").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/constancias/*/pdf").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                // incapacidades
                .antMatchers(HttpMethod.POST, "/api/incapacidades").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/incapacidades").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                .antMatchers(HttpMethod.GET, "/api/incapacidades/*").hasAnyRole(EMPLEADO, JEFATURA, RRHH)
                // empleados (solo RRHH)
                .antMatchers(HttpMethod.GET, "/api/empleados").hasRole(RRHH)
                .antMatchers(HttpMethod.GET, "/api/empleados/*").hasRole(RRHH)
                // todo lo demás, denegado
                .anyRequest().denyAll();

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ApiConfig.origenesCors());
        config.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setExposedHeaders(Arrays.asList("Location", "Content-Disposition"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }
}
