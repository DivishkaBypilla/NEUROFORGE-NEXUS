package com.neuroforge.neuroforge.nexus.config;

import com.neuroforge.neuroforge.nexus.entities.enums.Permission;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;

import java.util.*;

import static com.neuroforge.neuroforge.nexus.entities.enums.Permission.*;
import static com.neuroforge.neuroforge.nexus.entities.enums.Role.*;

public final class RolePermission {

    private static final Map<Role, Set<Permission>> MAP = new EnumMap<>(Role.class);

    static {
        MAP.put(ADMIN, EnumSet.allOf(Permission.class));

        MAP.put(PROJECT_MANAGER, EnumSet.of(
                PROJECT_CREATE, PROJECT_READ, PROJECT_UPDATE, PROJECT_ASSIGN_TEAM,
                SPRINT_CREATE, SPRINT_READ, SPRINT_UPDATE,
                TASK_CREATE, TASK_ASSIGN,
                RELEASE_CREATE, RELEASE_APPROVE, RELEASE_READ,
                MONITORING_READ,
                USER_MANAGE
        ));

        MAP.put(DEVELOPER, EnumSet.of(
                PROJECT_READ, SPRINT_READ,
                TASK_UPDATE_STATUS,
                PIPELINE_TRIGGER, PIPELINE_READ,
                MONITORING_READ
        ));

        MAP.put(TESTER, EnumSet.of(
                PROJECT_READ, SPRINT_READ,
                TASK_UPDATE_STATUS,
                PIPELINE_READ,
                RELEASE_READ
        ));

        MAP.put(DEVOPS_ENGINEER, EnumSet.of(
                PROJECT_READ,
                PIPELINE_TRIGGER, PIPELINE_READ, PIPELINE_ROLLBACK,
                RELEASE_READ,
                MONITORING_READ, MONITORING_ACK_ALERT
        ));
    }

    public static Set<Permission> forRole(Role role) {
        return MAP.getOrDefault(role, Set.of());
    }

    private RolePermission() {}
}