package com.dxm.anymock.manager.biz.service;

import com.dxm.anymock.manager.biz.model.request.CallLogRequest;
import com.dxm.anymock.manager.biz.model.response.dto.HttpInterfaceCallLogDTO;
import com.dxm.anymock.manager.biz.model.response.dto.PagingDataDTO;

import java.util.List;

public interface HttpInterfaceCallLogService {
    List<HttpInterfaceCallLogDTO> selectByInterfaceId(Long httpInterfaceId);

    PagingDataDTO<HttpInterfaceCallLogDTO> selectByInterfaceIdPaging(CallLogRequest request);
}
