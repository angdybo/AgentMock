package com.dxm.anymock.common.dal.dao.impl;

import com.dxm.anymock.common.dal.dao.HttpInterfaceCallLogDao;
import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDO;
import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDOExample;
import com.dxm.anymock.common.dal.mapper.auto.HttpInterfaceCallLogDOMapper;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HttpInterfaceCallLogDaoImpl implements HttpInterfaceCallLogDao {

    @Autowired
    private HttpInterfaceCallLogDOMapper httpInterfaceCallLogDOMapper;

    @Override
    public void insert(HttpInterfaceCallLogDO httpInterfaceCallLogDO) {
        httpInterfaceCallLogDOMapper.insert(httpInterfaceCallLogDO);
    }

    @Override
    public List<HttpInterfaceCallLogDO> selectByInterfaceId(Long httpInterfaceId) {
        HttpInterfaceCallLogDOExample example = new HttpInterfaceCallLogDOExample();
        example.createCriteria().andHttpInterfaceIdEqualTo(httpInterfaceId);
        example.setOrderByClause("gmt_create desc");
        return httpInterfaceCallLogDOMapper.selectByExample(example);
    }

    @Override
    public List<HttpInterfaceCallLogDO> selectByInterfaceIdWithRowBounds(Long httpInterfaceId, RowBounds rowBounds) {
        HttpInterfaceCallLogDOExample example = new HttpInterfaceCallLogDOExample();
        example.createCriteria().andHttpInterfaceIdEqualTo(httpInterfaceId);
        example.setOrderByClause("gmt_create desc");
        return httpInterfaceCallLogDOMapper.selectByExampleWithRowBounds(example, rowBounds);
    }

    @Override
    public long countByInterfaceId(Long httpInterfaceId) {
        HttpInterfaceCallLogDOExample example = new HttpInterfaceCallLogDOExample();
        example.createCriteria().andHttpInterfaceIdEqualTo(httpInterfaceId);
        return httpInterfaceCallLogDOMapper.countByExample(example);
    }
}
