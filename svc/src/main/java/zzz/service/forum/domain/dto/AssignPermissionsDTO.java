package zzz.service.forum.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 角色分配权限请求DTO
 *
 * @author EricRen
 * @since 2026-04-02
 */
public class AssignPermissionsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private String roleid;

    /**
     * 权限ID列表
     */
    private List<String> permissionids;

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    public List<String> getPermissionids() {
        return permissionids;
    }

    public void setPermissionids(List<String> permissionids) {
        this.permissionids = permissionids;
    }
}
