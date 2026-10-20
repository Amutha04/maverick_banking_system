package com.springboot.BankingSystem.config;

import com.springboot.BankingSystem.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final MyUserDetailsService myUserDetailsService;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // log in API
                        .requestMatchers(HttpMethod.GET,"/api/auth/login").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/admin/add").denyAll()

                        // CustomerController
                        .requestMatchers(HttpMethod.POST,"/api/customer/add").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/api/customer/delete/{customerId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/customer/all").hasAnyAuthority("ADMIN","EXECUTIVE")
                        .requestMatchers(HttpMethod.GET,"/api/customer/{customerId}").hasAnyAuthority("ADMIN","EXECUTIVE")
                        .requestMatchers(HttpMethod.GET,"/api/customer").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/customer/kyc-update/{customerId}").hasAuthority("EXECUTIVE")

                        // ExecutiveController
                        .requestMatchers(HttpMethod.POST,"/api/executive/add").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/executive/delete/{executiveId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/executive/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/executive/{executiveId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/executive").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.PUT,"/api/executive/update-jobtitle/{executiveId}").hasAuthority("ADMIN")

                        // AccountController
                        .requestMatchers(HttpMethod.POST,"/api/account/request").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/account/approve/{accountId}").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.PUT,"/api/account/reject/{accountId}").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.GET,"/api/account/all").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/account/get/{customerId}").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/account/{accountId}").hasAuthority("CUSTOMER")
                        .anyRequest().permitAll()
                );
        // Add jwt filter before Spring's Auth Filter
        http.addFilterBefore(jwtFilter , UsernamePasswordAuthenticationFilter.class);

        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationProvider getProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(myUserDetailsService);
        authenticationProvider.setPasswordEncoder(getEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}
