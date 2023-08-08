package com.example.demo.config.security;

import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.jwt.AuthTokenFilter;
import com.example.demo.payLoad.Message;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableScheduling
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.remember_me.key}")
    private String rememberMeKey;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Password encoder, để Spring Security sử dụng mã hóa mật khẩu người dùng
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        return new TokenBasedRememberMeServices(rememberMeKey, new CustomerUserDetailsService());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            Account account = accountRepository.findByUsername(username);
            User user = userRepository.findByAccount(account);
            if (account == null) throw new UsernameNotFoundException(username + " not found");
            return new CustomerUserDetails(user);
        }).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/auth/**", "/user/**").permitAll()
                .antMatchers("/employee/**").hasAnyAuthority("ADMIN", "EMPLOYEE")
                .antMatchers("/admin/**", "/cronjob/**").hasAnyAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .anonymous().authorities("ANONYMOUS");

        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        http.rememberMe()
                .key(rememberMeKey)
                .rememberMeServices(rememberMeServices())
                .userDetailsService(userDetailsService())
                .tokenValiditySeconds(86400); // 1 ngày

//        http.exceptionHandling()
//                .authenticationEntryPoint((request, response, authException) -> {
//                    Message<?> message = new Message<>("Authentication failed!", HttpStatus.UNAUTHORIZED, null);
//                    response.setContentType("application/json");
//                    response.getOutputStream().println(new ObjectMapper().writeValueAsString(message));
//                });
    }

}
