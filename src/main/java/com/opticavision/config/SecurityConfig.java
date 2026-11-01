
package com.opticavision.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            PermisoCrearUsuarios permisoCrearUsuarios
    ) throws Exception {

        http.authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/",
                        "/index.html",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico"
                ).permitAll()
.requestMatchers(
    "/gestion-usuarios/usuarios.css",
    "/gestion-usuarios/usuarios.js"
).hasAnyRole("ADMIN", "VENDEDOR")
                
                .requestMatchers(
                        "/admin/**",
                        "/api/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers(
    HttpMethod.GET,
    "/api/gestion-usuarios/contexto",
    "/api/gestion-usuarios/vendedores"
).access((authentication, context) ->
    new AuthorizationDecision(
        permisoCrearUsuarios.permitido(authentication.get())
    )
)

.requestMatchers(
    HttpMethod.POST,
    "/api/gestion-usuarios/vendedores"
).access((authentication, context) ->
    new AuthorizationDecision(
        permisoCrearUsuarios.permitido(authentication.get())
    )
)

                .requestMatchers(
                        "/vendedor/crear-vendedores.html",
                        "/gestion-usuarios/vendedores.html"
                ).access((authentication, context) ->
                        new AuthorizationDecision(
                                permisoCrearUsuarios.permitido(
                                        authentication.get()
                                )
                        )
                )

                .requestMatchers(
                        "/gestion-usuarios/**",
                        "/api/gestion-usuarios/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        "/vendedor/**",
                        "/api/vendedor/**"
                ).hasAnyRole("ADMIN", "VENDEDOR")

                .requestMatchers(
                        "/cliente/**",
                        "/api/cliente/**"
                ).hasRole("CLIENTE")

                .anyRequest().authenticated()

        ).formLogin(login -> login
                .defaultSuccessUrl("/inicio", false)
        ).logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        ).csrf(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
