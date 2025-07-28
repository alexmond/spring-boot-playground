package cloud.sample.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@ConfigurationProperties("users")
@Data
public class Users {
    public List<AdminUser> adminUsers;

    public Collection<UserDetails> getUserDetails(){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Collection<UserDetails> userDetails = new ArrayList<>();
        for(AdminUser adminUser : adminUsers){
            userDetails.add(User.withUsername(adminUser.getUsername()).password(adminUser.getPassword()).roles("USER").build());
        }
        return userDetails;
    }

}
