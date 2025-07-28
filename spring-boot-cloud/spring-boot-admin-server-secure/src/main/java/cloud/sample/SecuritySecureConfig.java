package cloud.sample;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import jakarta.servlet.DispatcherType;
import cloud.sample.config.Users;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class SecuritySecureConfig {

    private final AdminServerProperties adminServer;

    private final Users users;

    public SecuritySecureConfig(AdminServerProperties adminServer, Users users) {
        this.adminServer = adminServer;
        this.users = users;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(this.adminServer.path("/"));
        http.csrf(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests((authorizeRequests) -> authorizeRequests //
                        .requestMatchers(this.adminServer.path("/assets/**")).permitAll() // (1)
                        .requestMatchers(this.adminServer.path("/actuator/**")).permitAll()
                        .requestMatchers(this.adminServer.path("/login")).permitAll()
                        .requestMatchers(this.adminServer.path("/instances")).permitAll()
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll() // https://github.com/spring-projects/spring-security/issues/11027
                        .anyRequest()
                        .authenticated()) // (2)
                .formLogin(
                        (formLogin) -> formLogin.loginPage(this.adminServer.path("/login")).successHandler(successHandler)) // (3)
                .logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")))
                .httpBasic(Customizer.withDefaults()); // (4)

        http.rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600));

        return http.build();

    }

    // Required to provide UserDetailsService for "remember functionality"
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(users.getUserDetails());
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder();}

    @Bean
    public AuditEventRepository InMemoryAuditEventRepository(){
        return new InMemoryAuditEventRepository();
    }

}