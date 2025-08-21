package ufgfans.com.ufgfans.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import ufgfans.com.ufgfans.Handler.CustomAuthenticationSuccessHandler;
import ufgfans.com.ufgfans.Service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/registro",
                    "/registro/**",
                    "/verificar",
                    "/verificar/**",
                    "/completar-registro",
                    "/completar-registro/**",
                    "/login",
                    "/google-auth/setup",
                    "/google-auth/validate",
                    "/css/**",
                    "/js/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .failureUrl("/login?error=true")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    response.sendRedirect("/login?error=true&username=" + username + "&password=" + password);
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)  // ⚡ invalida sesión al cerrar sesión
                .deleteCookies("JSESSIONID")  // ⚡ borra cookies
                .permitAll()
            )
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?timeout") // ⚡ redirige cuando la sesión expira
                .maximumSessions(1)                  // opcional: limitar sesiones por usuario
                .expiredUrl("/login?expired")       // opcional: url cuando expira otra sesión
            );

        return http.build();
    }

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
            .and()
            .build();
    }

}

