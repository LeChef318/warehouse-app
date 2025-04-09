package ch.hoffmann.jan.warehouse.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            Collection<GrantedAuthority> authorities = Stream.concat(
                    jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                    extractRoles(jwt).stream()
            ).collect(Collectors.toSet());

            return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
        } catch (Exception e) {
            // Log the error but don't throw it
            System.err.println("Error converting JWT: " + e.getMessage());
            // Return a token with minimal authorities
            return new JwtAuthenticationToken(jwt, Collections.emptyList(), jwt.getSubject());
        }
    }

    private Collection<? extends GrantedAuthority> extractRoles(Jwt jwt) {
        Set<GrantedAuthority> authorities = extractRealmRoles(jwt);
        authorities.addAll(extractResourceRoles(jwt));
        return authorities;
    }

    private Set<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || realmAccess.isEmpty()) {
                return Collections.emptySet();
            }

            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            if (roles == null || roles.isEmpty()) {
                return Collections.emptySet();
            }

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.err.println("Error extracting realm roles: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    private Set<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        try {
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess == null || resourceAccess.isEmpty()) {
                return Collections.emptySet();
            }

            Map<String, Object> resource = (Map<String, Object>) resourceAccess.get("warehouse-app");
            if (resource == null || resource.isEmpty()) {
                return Collections.emptySet();
            }

            Collection<String> roles = (Collection<String>) resource.get("roles");
            if (roles == null || roles.isEmpty()) {
                return Collections.emptySet();
            }

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.err.println("Error extracting resource roles: " + e.getMessage());
            return Collections.emptySet();
        }
    }
}

