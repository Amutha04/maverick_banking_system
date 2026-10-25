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
                        .requestMatchers(HttpMethod.GET,"/api/customer/filter").hasAnyAuthority("ADMIN","EXECUTIVE")
                        .requestMatchers(HttpMethod.GET,"/api/customer/{customerId}").hasAnyAuthority("ADMIN","EXECUTIVE")
                        .requestMatchers(HttpMethod.GET,"/api/customer").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/customer/kyc-update/{customerId}").hasAuthority("EXECUTIVE")

                        // ExecutiveController
                        .requestMatchers(HttpMethod.POST,"/api/executive/add").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/executive/delete/{executiveId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/executive/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/executive/filter").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/executive/{executiveId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/executive").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.PUT,"/api/executive/update-jobtitle/{executiveId}").hasAuthority("ADMIN")

                        // AccountController
                        .requestMatchers(HttpMethod.POST,"/api/account/request").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/account/request/all").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/account/request/filter").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/account/request/{accountRequestId}").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/account/request").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/account").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/account/open-request/approve/{accountRequestId}").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.PUT,"/api/account/request/reject/{accountRequestId}").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.PUT,"/api/account/close-request/approve/{accountRequestId}").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.GET,"/api/account/all").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/account/get/{customerId}").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/account/{accountId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/account/{accountId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/account/activate/{accountId}").hasAuthority("ADMIN")

                        // TransactionController
                        .requestMatchers(HttpMethod.POST,"/api/transaction/add/{accountId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/transaction/all").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/transaction/filter").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/transaction/get/{accountId}").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/transaction/{accountId}").hasAuthority("CUSTOMER")

                        // BranchController
                        .requestMatchers(HttpMethod.GET,"/api/branch/all").hasAnyAuthority("EXECUTIVE", "ADMIN")

                        // BeneficiaryController
                        .requestMatchers(HttpMethod.POST,"/api/beneficiary/add").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE,"/api/beneficiary/delete/{beneficiaryId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/beneficiary").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.POST,"/api/beneficiary/transfer/{accountId}/{beneficiaryId}").hasAuthority("CUSTOMER")

                        // LoanController
                        .requestMatchers(HttpMethod.POST,"/api/loan/request/{accountId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/loan/approve/{loanId}").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.PUT,"/api/loan/reject/{loanId}").hasAuthority("EXECUTIVE")
                        .requestMatchers(HttpMethod.PUT,"/api/loan/accept/{loanId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/loan/decline/{loanId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,"/api/loan/repay/{loanId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/loan/all").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/loan/filter").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/loan/get/{customerId}").hasAnyAuthority("EXECUTIVE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/loan/{loanId}").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,"/api/loan").hasAuthority("CUSTOMER")

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
