package com.proyecto.AccesoUsuarios.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // 1. Recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**").permitAll()
                    
                // 2. Rutas Públicas
                .requestMatchers("/", "/index", "/home", "/login", "/registro", "/registro/**", "/guardarUsuario").permitAll()

                // Permite acceso a recuperación sin loguearse
                .requestMatchers("/auth/**").permitAll()

                .requestMatchers("/estudiante/guardar-alerta").authenticated()

                // 3. Rutas EXCLUSIVAS de ADMINISTRADOR (Gestión de usuarios y sistema)
                .requestMatchers("/admin/**", "/usuarios/**").hasRole("ADMIN")

                // 4. Rutas COMPARTIDAS: ADMIN e INSTITUCION (Crear convocatorias)
                .requestMatchers("/convocatorias/nueva", "/convocatorias/guardar")
                    .hasAnyRole("ADMIN", "INSTITUCION")
                
                // 5. Rutas EXCLUSIVAS de INSTITUCION (Dashboard, gestión de inscripciones)
                .requestMatchers("/institucion/**").hasRole("INSTITUCION")

                // 6. Rutas de USUARIO/ESTUDIANTE
                .requestMatchers("/convocatorias/inscribirse/**", "/convocatorias/mis-inscripciones").hasRole("USER")

                // 7. Rutas Comunes (Dashboard, Perfil, PDF, etc.)
                .anyRequest().authenticated()
                
            )
            .formLogin(form -> form
                .loginPage("/login") 
                .defaultSuccessUrl("/dashboard", true) // El DashboardController decidirá a dónde mandarlos
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}