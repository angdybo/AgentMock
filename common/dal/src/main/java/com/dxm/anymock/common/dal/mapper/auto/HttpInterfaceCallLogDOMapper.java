package com.dxm.anymock.common.dal.mapper.auto;

import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDO;
import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDOExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface HttpInterfaceCallLogDOMapper {
    long countByExample(HttpInterfaceCallLogDOExample example);

    int deleteByExample(HttpInterfaceCallLogDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(HttpInterfaceCallLogDO record);

    int insertSelective(HttpInterfaceCallLogDO record);

    List<HttpInterfaceCallLogDO> selectByExampleWithRowBounds(HttpInterfaceCallLogDOExample example, RowBounds rowBounds);

    List<HttpInterfaceCallLogDO> selectByExample(HttpInterfaceCallLogDOExample example);

    HttpInterfaceCallLogDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") HttpInterfaceCallLogDO record, @Param("example") HttpInterfaceCallLogDOExample example);

    int updateByExample(@Param("record") HttpInterfaceCallLogDO record, @Param("example") HttpInterfaceCallLogDOExample example);

    int updateByPrimaryKeySelective(HttpInterfaceCallLogDO record);

    int updateByPrimaryKey(HttpInterfaceCallLogDO record);
}
