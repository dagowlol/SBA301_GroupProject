package hoang.com.auction_system_be.security;
import com.fasterxml.jackson.annotation.JsonIgnore;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
@AllArgsConstructor
@Getter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    private final Long id;
    private final String email;
    private final UserStatus status;
    @JsonIgnore
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                user.getPasswordHash(),
                authorities
        );
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    @Override
    public String getPassword() {
        return this.password;
    }
    @Override
    public String getUsername() {
        return this.email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return this.status != UserStatus.LOCKED;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return this.status == UserStatus.ACTIVE || this.status == UserStatus.PENDING;
    }
}
