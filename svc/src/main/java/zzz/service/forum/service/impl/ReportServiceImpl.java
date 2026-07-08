package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.ReportEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.ReportPojo;
import zzz.service.forum.mapper.ReportMapper;
import zzz.service.forum.service.ReportService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 举报记录表(Report)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:21:59
 */
@Service("reportService")
public class ReportServiceImpl implements ReportService {
    @Resource
    private ReportMapper reportMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public ReportPojo getEntity(String key) {
        return this.reportMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<ReportPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<ReportPojo> lst = reportMapper.getPageList(queryParam);
            PageInfo<ReportPojo> pageInfo = new PageInfo<ReportPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param reportPojo 实例对象
     * @return 实例对象
     */
    @Override
    public ReportPojo insert(ReportPojo reportPojo) {
        //初始化NULL字段
        if (reportPojo.getTargettype() == null) reportPojo.setTargettype(0);
        if (reportPojo.getReason() == null) reportPojo.setReason("");
        if (reportPojo.getDescription() == null) reportPojo.setDescription("");
        if (reportPojo.getStatus() == null) reportPojo.setStatus(0);
        if (reportPojo.getCreatedat() == null) reportPojo.setCreatedat(new Date());
        ReportEntity reportEntity = new ReportEntity();
        BeanUtils.copyProperties(reportPojo, reportEntity);
        //生成雪花id
        reportEntity.setId(snowflakeIdGenerator.nextId());

        this.reportMapper.insert(reportEntity);
        return this.getEntity(String.valueOf(reportEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param reportPojo 实例对象
     * @return 实例对象
     */
    @Override
    public ReportPojo update(ReportPojo reportPojo) {
        ReportEntity reportEntity = new ReportEntity();
        BeanUtils.copyProperties(reportPojo, reportEntity);
        this.reportMapper.update(reportEntity);
        return this.getEntity(String.valueOf(reportEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.reportMapper.delete(key);
    }


}
