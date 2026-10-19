package com.webelement.taskapp.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import com.webelement.taskapp.jwttoken.AppUser;
import com.webelement.taskapp.jwttoken.CurrentUser;
import com.webelement.taskapp.repo.UserLoginRepository;
import lombok.RequiredArgsConstructor;

@Service("perm")
@RequiredArgsConstructor
public class PermissionService {

    private final UserLoginRepository userLoginRepository;

    /** action: view, add, edit, delete, export */
    public boolean has(int moduleId, String action) {
        AppUser u = CurrentUser.get();
        if (u.isAdmin()) {
            return true;
        }

        String column;
        switch (action) {
            case "add":    column = "s_add"; break;
            case "edit":   column = "s_edit"; break;
            case "delete": column = "s_delete"; break;
            case "export": column = "s_exportexcel"; break;
            default:       column = "s_view";
        }

        for (Map<String, Object> row : userLoginRepository.getModuleListByName(u.getUserId())) {
            Object id = row.get("i_moduleid");
            if (id instanceof Number && ((Number) id).intValue() == moduleId) {
                Object v = row.get(column);
                return v != null && "Y".equalsIgnoreCase(v.toString());
            }
        }
        return false;
    }

    public boolean has(int moduleId) {
        return has(moduleId, "view");
    }
}