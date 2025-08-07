package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.domain.dto.CategoryDTO;
import com.blog.domain.dto.SearchCategoryDTO;
import com.blog.domain.entity.Category;
import com.blog.domain.response.ResponseResult;
import com.blog.domain.vo.CategoryVO;

import java.util.List;

/**
 * @author haibara
 * @description 文章分类服务接口
 * @since 2025/7/27
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询所有分类
     *
     * @return vo
     */
    List<CategoryVO> listAllCategory();

    /**
     * 添加分类
     * @param categoryDTO 分类
     * @return 是否成功
     */
    ResponseResult<Void> addCategory(CategoryDTO categoryDTO);

    /**
     * 搜索分类
     * @param searchCategoryDTO 搜索标签DTO
     * @return 分类列表
     */
    List<CategoryVO> searchCategory(SearchCategoryDTO searchCategoryDTO);

    /**
     * 根据id查询
     * @param id id
     * @return 标签
     */
    CategoryVO getCategoryById(Long id);

    /**
     * 新增或修改标签
     * @param categoryDTO 标签DTO
     * @return 是否成功
     */
    ResponseResult<Void> addOrUpdateCategory(CategoryDTO categoryDTO);

    /**
     * 根据id删除
     * @param ids id
     * @return 是否成功
     */
    ResponseResult<Void> deleteCategoryByIds(List<Long> ids);

    /**
     * 从数据库查询所有分类（不走缓存）
     * @return 分类列表
     */
    List<CategoryVO> listAllCategoryFromDB();
}
