package ch.wisv.areafiftylan.integration;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.wisv.areafiftylan.security.SecurityConfiguration.HIERARCHY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoleHierarchyTest extends XAuthIntegrationTest {

    @Test
    public void SecurityRoleUserTest() {
        Set<? extends GrantedAuthority> roles = createUser().getAuthorities();
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(HIERARCHY);
        Collection<GrantedAuthority> reachableGrantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(roles);
        List<String> roleStrings = reachableGrantedAuthorities.
                stream().
                map(GrantedAuthority::getAuthority).
                collect(Collectors.toList());
        assertTrue(roleStrings.contains("ROLE_USER"));
        assertFalse(roleStrings.contains("ROLE_OPERATOR"));
        assertFalse(roleStrings.contains("ROLE_COMMITTEE"));
        assertFalse(roleStrings.contains("ROLE_ADMIN"));
    }

    @Test
    public void SecurityRoleOperatorTest() {
        Set<? extends GrantedAuthority> roles = createOperator().getAuthorities();
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(HIERARCHY);
        Collection<GrantedAuthority> reachableGrantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(roles);
        List<String> roleStrings = reachableGrantedAuthorities.
                stream().
                map(GrantedAuthority::getAuthority).
                collect(Collectors.toList());
        assertTrue(roleStrings.contains("ROLE_USER"));
        assertTrue(roleStrings.contains("ROLE_OPERATOR"));
        assertFalse(roleStrings.contains("ROLE_COMMITTEE"));
        assertFalse(roleStrings.contains("ROLE_ADMIN"));
    }

    @Test
    public void SecurityRoleCommitteeTest() {
        Set<? extends GrantedAuthority> roles = createCommitteeMember().getAuthorities();
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(HIERARCHY);
        Collection<GrantedAuthority> reachableGrantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(roles);
        List<String> roleStrings = reachableGrantedAuthorities.
                stream().
                map(GrantedAuthority::getAuthority).
                collect(Collectors.toList());
        assertTrue(roleStrings.contains("ROLE_USER"));
        assertTrue(roleStrings.contains("ROLE_OPERATOR"));
        assertTrue(roleStrings.contains("ROLE_COMMITTEE"));
        assertFalse(roleStrings.contains("ROLE_ADMIN"));
    }

    @Test
    public void SecurityRoleAdminTest() {
        Set<? extends GrantedAuthority> roles = createAdmin().getAuthorities();
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(HIERARCHY);
        Collection<GrantedAuthority> reachableGrantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(roles);
        List<String> roleStrings = reachableGrantedAuthorities.
                stream().
                map(GrantedAuthority::getAuthority).
                collect(Collectors.toList());
        assertTrue(roleStrings.contains("ROLE_USER"));
        assertTrue(roleStrings.contains("ROLE_OPERATOR"));
        assertTrue(roleStrings.contains("ROLE_COMMITTEE"));
        assertTrue(roleStrings.contains("ROLE_ADMIN"));
    }
}
