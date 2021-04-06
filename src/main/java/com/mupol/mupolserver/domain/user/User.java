package com.mupol.mupolserver.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.instrument.Instrument;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user")
public class User extends BaseTime implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String snsId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SnsType provider;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false)
    private boolean isAgreed;

    @Column(nullable = false)
    private boolean isMajor;

    @Column(nullable = false)
    private Role role;

    private String email; // 현재 db 스키마에 없음
    private String bio;
    private String profileImageUrl;
    private String bgImageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Instrument> favoriteInstrument = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.getKey()));
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public String getPassword() {
        return null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isEnabled() {
        return false;
    }


    @Getter
    @RequiredArgsConstructor
    public enum Role {

        ADMIN("ROLE_ADMIN", "관리자"),
        USER("ROLE_USER", "사용자");

        private final String key;
        private final String title;
    }

}
