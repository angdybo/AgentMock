package com.dxm.anymock.manager.biz.model.request;

import javax.validation.constraints.NotNull;

public class CallLogRequest extends BasePagingRequest {
    @NotNull
    private Long interfaceId;

    public Long getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(Long interfaceId) {
        this.interfaceId = interfaceId;
    }
}
