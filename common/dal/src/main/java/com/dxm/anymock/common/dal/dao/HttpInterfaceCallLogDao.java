package com.dxm.anymock.common.dal.dao;

import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDO;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface HttpInterfaceCallLogDao {
    void insert(HttpInterfaceCallLogDO httpInterfaceCallLogDO);

    List<HttpInterfaceCallLogDO> selectByInterfaceId(Long httpInterfaceId);

    List<HttpInterfaceCallLogDO> selectByInterfaceIdWithRowBounds(Long httpInterfaceId, RowBounds rowBounds);

    long countByInterfaceId(Long httpInterfaceId);
}
