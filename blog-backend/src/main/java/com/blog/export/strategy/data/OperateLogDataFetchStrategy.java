package com.blog.export.strategy.data;

import com.blog.domain.vo.LogVO;
import com.blog.domain.vo.PageVO;
import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.service.LogService;
import com.blog.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 操作日志数据获取策略
 * 
 * @author haibara
 * @description 从操作日志服务获取操作日志列表数据
 * @since 2025/1/21
 */
@Component
public class OperateLogDataFetchStrategy implements DataFetchStrategy<LogVO> {
    
    @Resource
    private LogService logService;
    
    @Override
    public List<LogVO> fetchData() {
        // 调用LogService的searchLog方法获取操作日志列表
        // 注意：这里需要处理分页，我们获取前1000条记录用于导出
        @SuppressWarnings("unchecked")
        PageVO<List<LogVO>> pageResult = (PageVO<List<LogVO>>) logService.searchLog(null, 1L, 1000L);
        return pageResult.getPage();
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.OPERATE_LOG;
    }
    
    @Override
    public Class<LogVO> getDataClass() {
        return LogVO.class;
    }
    
    @Override
    public boolean hasPermission() {
        // 检查是否有操作日志查看权限
        return SecurityUtils.hasAnyAuthority("system:log:list");
    }
}