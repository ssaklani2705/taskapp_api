package com.webelement.taskapp.jwttoken;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.webelement.taskapp.common.CommonFunction;

import lombok.RequiredArgsConstructor;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity            // if this annotation isn't found, use @EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtRequestFilter jwtFilter;
	private final CommonFunction commonFunction;

	// New Added
@Bean
public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder() {
        @Override
        public String encode(CharSequence rawPassword) {
            return rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            String decrypted = null;
            try {
                decrypted = commonFunction.decipher(encodedPassword);
            } catch (Exception e) {
            }
            return rawPassword.toString().equals(decrypted);
        }
    };
}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
	// -----------------------

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()).cors(cors -> {
		}).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//				.authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("/ws/**")).permitAll()
//						.requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()
//						.requestMatchers(new AntPathRequestMatcher("/admin/**")).permitAll()
//						.requestMatchers(new AntPathRequestMatcher("/uploads/**")).permitAll()
//						.requestMatchers(new AntPathRequestMatcher("/**", "OPTIONS")).permitAll().anyRequest()
//						.authenticated());
		.authorizeHttpRequests(auth -> auth
			    .requestMatchers(new AntPathRequestMatcher("/**", "OPTIONS")).permitAll()
			    .requestMatchers(new AntPathRequestMatcher("/auth/login")).permitAll()
			    .requestMatchers(new AntPathRequestMatcher("/auth/captcha")).permitAll()
			    .requestMatchers(new AntPathRequestMatcher("/auth/forgotpasswordMail")).permitAll()
			    .requestMatchers(new AntPathRequestMatcher("/auth/forgotpassword")).permitAll()
			    .requestMatchers(new AntPathRequestMatcher("/ws/**")).permitAll()      // revisit in step 7
			    .requestMatchers(new AntPathRequestMatcher("/uploads/**")).permitAll() // revisit in step 7
			    .requestMatchers(new AntPathRequestMatcher("/admin/**")).permitAll()
			    .anyRequest().authenticated());

		http.exceptionHandling(ex -> ex
			    .authenticationEntryPoint((req, res, e) -> {
			        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			        res.setContentType("application/json");
			        res.getWriter().write("{\"error\":\"Unauthorized\"}");
			    })
			    .accessDeniedHandler((req, res, e) -> {
			        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			        res.setContentType("application/json");
			        res.getWriter().write("{\"error\":\"Forbidden\"}");
			    }));
		
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
