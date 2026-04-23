package com.dxm.anymock.manager.biz.model.response.dto;

import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDO;

import java.util.Date;
import java.util.List;

public class HttpInterfaceCallLogDTO extends HttpInterfaceCallLogDO {
    private List<Long> path;

    public List<Long> getPath() {
        return path;
    }

    public void setPath(List<Long> path) {
        this.path = path;
    }
}
