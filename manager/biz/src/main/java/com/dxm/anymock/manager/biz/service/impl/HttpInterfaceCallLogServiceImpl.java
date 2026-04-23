package com.dxm.anymock.manager.biz.service.impl;

import com.dxm.anymock.common.base.enums.ResultCode;
import com.dxm.anymock.common.base.exception.BizException;
import com.dxm.anymock.common.dal.dao.HttpInterfaceCallLogDao;
import com.dxm.anymock.common.dal.dao.HttpInterfaceDao;
import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDO;
import com.dxm.anymock.common.dal.model.HttpInterfaceBO;
import com.dxm.anymock.manager.biz.RowBoundsConverter;
import com.dxm.anymock.manager.biz.model.request.CallLogRequest;
import com.dxm.anymock.manager.biz.security.PrivilegeVerifier;
import com.dxm.anymock.manager.biz.service.HttpInterfaceCallLogService;
import com.dxm.anymock.manager.biz.model.response.dto.HttpInterfaceCallLogDTO;
import com.dxm.anymock.manager.biz.model.response.dto.PagingDataDTO;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class HttpInterfaceCallLogServiceImpl implements HttpInterfaceCallLogService {

    @Autowired
    private HttpInterfaceCallLogDao httpInterfaceCallLogDao;

    @Autowired
    private HttpInterfaceDao httpInterfaceDao;

    @Override
    public List<HttpInterfaceCallLogDTO> selectByInterfaceId(Long httpInterfaceId) {
        HttpInterfaceBO httpInterfaceBO = httpInterfaceDao.queryById(httpInterfaceId);
        if (httpInterfaceBO == null) {
            throw new BizException(ResultCode.NOT_FOUND_HTTP_INTERFACE);
        }
        if (!PrivilegeVerifier.hasPermission(httpInterfaceBO.getAccessAuthority())) {
            throw new BizException(ResultCode.PERMISSION_DENIED);
        }

        List<HttpInterfaceCallLogDO> logDOList = httpInterfaceCallLogDao.selectByInterfaceId(httpInterfaceId);
        List<HttpInterfaceCallLogDTO> logDTOList = new LinkedList<>();
        for (HttpInterfaceCallLogDO logDO : logDOList) {
            HttpInterfaceCallLogDTO dto = new HttpInterfaceCallLogDTO();
            BeanUtils.copyProperties(logDO, dto);
            logDTOList.add(dto);
        }
        return logDTOList;
    }

    @Override
    public PagingDataDTO<HttpInterfaceCallLogDTO> selectByInterfaceIdPaging(CallLogRequest request) {
        HttpInterfaceBO httpInterfaceBO = httpInterfaceDao.queryById(request.getInterfaceId());
        if (httpInterfaceBO == null) {
            throw new BizException(ResultCode.NOT_FOUND_HTTP_INTERFACE);
        }
        if (!PrivilegeVerifier.hasPermission(httpInterfaceBO.getAccessAuthority())) {
            throw new BizException(ResultCode.PERMISSION_DENIED);
        }

        RowBounds rowBounds = RowBoundsConverter.convert(request);
        List<HttpInterfaceCallLogDO> logDOList = httpInterfaceCallLogDao.selectByInterfaceIdWithRowBounds(request.getInterfaceId(), rowBounds);

        PagingDataDTO<HttpInterfaceCallLogDTO> pagingDataDTO = new PagingDataDTO<>(request);
        pagingDataDTO.setTotal(httpInterfaceCallLogDao.countByInterfaceId(request.getInterfaceId()));
        List<HttpInterfaceCallLogDTO> logDTOList = new LinkedList<>();
        for (HttpInterfaceCallLogDO logDO : logDOList) {
            HttpInterfaceCallLogDTO dto = new HttpInterfaceCallLogDTO();
            BeanUtils.copyProperties(logDO, dto);
            logDTOList.add(dto);
        }
        pagingDataDTO.setList(logDTOList);
        return pagingDataDTO;
    }
}
