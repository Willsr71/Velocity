package com.velocitypowered.proxy.permission;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPermissionSubject implements PermissionSubject {
  protected PermissionFunction permissionFunction;
  private @MonotonicNonNull Map<String, Tristate> permissions;

  protected AbstractPermissionSubject(final PermissionFunction permissionFunction) {
    this.permissionFunction = permissionFunction;
  }

  @Override
  public void setPermission(final String permission, final Tristate value) {
    if (permissions == null) {
      permissions = new HashMap<>();
    }
    permissions.put(permission, value);
  }

  @Override
  public Tristate getPermissionValue(final String permission) {
    Tristate value = permissionFunction.getPermissionValue(permission);
    if (value == Tristate.UNDEFINED && permissions != null) {
      value = permissions.getOrDefault(permission, value);
    }
    return value;
  }
}
