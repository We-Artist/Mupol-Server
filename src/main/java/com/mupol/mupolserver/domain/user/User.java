package com.mupol.mupolserver.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.followers.Followers;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.sound.Sound;
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

    @Setter
    @Column(nullable = false)
    private String username;

    @Setter
    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false)
    private boolean terms;

    @Setter
    @Column(nullable = false)
    private boolean isMajor;

    @Column(nullable = false)
    private Role role;

    @Setter
    private String email; // 현재 db 스키마에 없음
    @Setter
    private String bio;
    @Setter
    private String profileImageUrl;
    @Setter
    private String bgImageUrl;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Instrument> favoriteInstrument = new ArrayList<>();

    @JsonIgnore
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
    private Set<Sound> sounds;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "to")
    private Set<Followers> followings;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "from")
    private Set<Followers> followers;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
    private Set<MonthlyGoal> goals;

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
