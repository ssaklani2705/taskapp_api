package com.webelement.taskapp.jwttoken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.webelement.taskapp.common.CommonFunction;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Autowired
	private JwtRequestFilter jwtFilter;

	@Autowired
	private CommonFunction commonFunction;

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
//                    logger.debug("Password decrypted in matcher");
				} catch (Exception e) {
//                    logger.error("Error decrypting password", e);
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

//	@Bean
//	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		http.csrf().disable().cors() // Enable CORS
//				.and().authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow preflight
//				.antMatchers("/auth/**").permitAll().antMatchers("/uploads/**").permitAll() // Login & Refresh token
//				.anyRequest().authenticated() // Protect all other APIs
//				.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//			return http.build();
//		}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

	    http
	        .csrf(csrf -> csrf.disable())
	        .cors(cors -> {})
	        .sessionManagement(session ->
	            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()
	            .requestMatchers(new AntPathRequestMatcher("/uploads/**")).permitAll()
	            .requestMatchers(new AntPathRequestMatcher("/**", "OPTIONS")).permitAll()
	            .anyRequest().authenticated()
	        );

	    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}
}
