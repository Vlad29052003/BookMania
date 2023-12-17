package nl.tudelft.sem.template.authentication.config;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.authentication.authentication.JwtAuthenticationEntryPoint;
import nl.tudelft.sem.template.authentication.authentication.JwtAuthenticationFilter;
import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The type Web security config.
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Getter
    @Setter(onMethod = @__({@Autowired})) // add autowired annotation on setter
    private transient UserDetailsService userDetailsService;
    private final transient JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final transient JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public WebSecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                             JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Password encoder password encoder.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordHashingService passwordHashEncoder() {
        return new PasswordHashingService(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/c/register", "/c/authenticate", "/c/unauthorized/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
