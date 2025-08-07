-- ==================== 弹窗管理功能菜单和权限 SQL ====================
-- 执行前请确认当前的ID值：
-- menu表当前最大ID: 73 (下一个使用74)
-- permission表当前最大ID: 162 (下一个使用163)
-- role_menu表当前最大ID: 1465 (下一个使用1466)
-- role_permission表当前最大ID: 284 (下一个使用285)

-- ==================== 1. 菜单数据 ====================

-- 插入弹窗管理菜单
INSERT INTO `sys_menu` VALUES (
    74,                           -- id
    '弹窗管理',                    -- title
    'NotificationOutlined',       -- icon (使用通知图标)
    '/system/popup',             -- path
    '/system/popup',             -- component
    '',                          -- redirect
    0,                           -- affix (不固定页签)
    1,                           -- parent_id (系统管理的菜单ID)
    'PopupManagement',           -- name
    0,                           -- hide_in_menu (不隐藏)
    NULL,                        -- url
    1,                           -- hide_in_breadcrumb (不隐藏面包屑)
    1,                           -- hide_children_in_menu (不隐藏子菜单)
    1,                           -- keep_alive (保活)
    NULL,                        -- target
    0,                           -- is_disable (不禁用)
    8,                           -- order_num (排序号)
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted (未删除)
);

-- ==================== 2. 权限数据 ====================

-- 弹窗列表权限
INSERT INTO `sys_permission` VALUES (
    163,                         -- id
    '弹窗列表',                   -- permission_desc
    'blog:popup:list',           -- permission_key
    74,                          -- menu_id
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted
);

-- 弹窗搜索权限
INSERT INTO `sys_permission` VALUES (
    164,                         -- id
    '弹窗搜索',                   -- permission_desc
    'blog:popup:search',         -- permission_key
    74,                          -- menu_id
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted
);

-- 弹窗添加权限
INSERT INTO `sys_permission` VALUES (
    165,                         -- id
    '弹窗添加',                   -- permission_desc
    'blog:popup:add',            -- permission_key
    74,                          -- menu_id
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted
);

-- 弹窗修改权限
INSERT INTO `sys_permission` VALUES (
    166,                         -- id
    '弹窗修改',                   -- permission_desc
    'blog:popup:update',         -- permission_key
    74,                          -- menu_id
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted
);

-- 弹窗详情权限
INSERT INTO `sys_permission` VALUES (
    167,                         -- id
    '弹窗详情',                   -- permission_desc
    'blog:popup:get',            -- permission_key
    74,                          -- menu_id
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted
);

-- 弹窗删除权限
INSERT INTO `sys_permission` VALUES (
    168,                         -- id
    '弹窗删除',                   -- permission_desc
    'blog:popup:delete',         -- permission_key
    74,                          -- menu_id
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted
);

-- 弹窗状态更新权限
INSERT INTO `sys_permission` VALUES (
    169,                         -- id
    '弹窗状态更新',               -- permission_desc
    'blog:popup:status:update',  -- permission_key
    74,                          -- menu_id
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted
);

-- 弹窗图片上传权限
INSERT INTO `sys_permission` VALUES (
    170,                         -- id
    '弹窗图片上传',               -- permission_desc
    'blog:popup:upload',         -- permission_key
    74,                          -- menu_id
    NOW(),                       -- create_time
    NOW(),                       -- update_time
    0                            -- is_deleted
);

-- ==================== 3. 角色菜单关联 ====================

-- 为管理员角色(role_id=1)分配弹窗管理菜单
INSERT INTO `sys_role_menu` VALUES (
    1466,                        -- id
    1,                           -- role_id (管理员角色)
    74,                          -- menu_id (弹窗管理菜单)
    0                            -- is_deleted
);

-- 为普通管理员角色(role_id=2)分配弹窗管理菜单
INSERT INTO `sys_role_menu` VALUES (
    1467,                        -- id
    2,                           -- role_id (普通管理员角色)
    74,                          -- menu_id (弹窗管理菜单)
    0                            -- is_deleted
);

-- ==================== 4. 角色权限关联 ====================

-- 为管理员角色(role_id=1)分配所有弹窗权限
INSERT INTO `sys_role_permission` VALUES (285, 1, 163);  -- 弹窗列表
INSERT INTO `sys_role_permission` VALUES (286, 1, 164);  -- 弹窗搜索
INSERT INTO `sys_role_permission` VALUES (287, 1, 165);  -- 弹窗添加
INSERT INTO `sys_role_permission` VALUES (288, 1, 166);  -- 弹窗修改
INSERT INTO `sys_role_permission` VALUES (289, 1, 167);  -- 弹窗详情
INSERT INTO `sys_role_permission` VALUES (290, 1, 168);  -- 弹窗删除
INSERT INTO `sys_role_permission` VALUES (291, 1, 169);  -- 弹窗状态更新
INSERT INTO `sys_role_permission` VALUES (292, 1, 170);  -- 弹窗图片上传

-- 为普通管理员角色(role_id=2)分配部分弹窗权限(不包括删除权限)
INSERT INTO `sys_role_permission` VALUES (293, 2, 163);  -- 弹窗列表
INSERT INTO `sys_role_permission` VALUES (294, 2, 164);  -- 弹窗搜索
INSERT INTO `sys_role_permission` VALUES (295, 2, 165);  -- 弹窗添加
INSERT INTO `sys_role_permission` VALUES (296, 2, 166);  -- 弹窗修改
INSERT INTO `sys_role_permission` VALUES (297, 2, 167);  -- 弹窗详情
INSERT INTO `sys_role_permission` VALUES (298, 2, 169);  -- 弹窗状态更新
INSERT INTO `sys_role_permission` VALUES (299, 2, 170);  -- 弹窗图片上传

-- ==================== 执行完成提示 ====================
-- 执行完成后，请检查：
-- 1. 菜单是否正确显示在后台管理系统中
-- 2. 权限是否正确分配给对应角色
-- 3. 用户是否能正常访问弹窗管理功能
-- 
-- parent_id=1 对应系统管理菜单
