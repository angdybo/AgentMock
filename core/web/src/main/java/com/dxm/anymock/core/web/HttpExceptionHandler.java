package com.dxm.anymock.core.web;

import com.dxm.anymock.common.base.enums.ResultCode;
import com.dxm.anymock.common.base.exception.BizException;
import com.dxm.anymock.common.base.ResultCodeTranslator;
import com.dxm.anymock.common.dal.dao.HttpInterfaceCallLogDao;
import com.dxm.anymock.common.dal.entity.HttpInterfaceCallLogDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

@ControllerAdvice
public class HttpExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpExceptionHandler.class);

    @Autowired
    private ResultCodeTranslator translator;

    @Autowired
    private HttpInterfaceCallLogDao httpInterfaceCallLogDao;

    private void recordErrorCallLog(HttpServletRequest request, HttpServletResponse response, ResultCode resultCode) {
        try {
            HttpInterfaceCallLogDO logDO = new HttpInterfaceCallLogDO();
            logDO.setHttpInterfaceId(request.getAttribute("_httpInterfaceId") != null
                    ? (Long) request.getAttribute("_httpInterfaceId") : null);
            logDO.setRequestUri(request.getRequestURI());
            logDO.setRequestMethod(request.getMethod());
            logDO.setResponseStatus(response.getStatus());
            logDO.setAsync("false");
            logDO.setGmtCreate(new Date());

            // 记录响应体为错误信息
            Map<String, String> resultMap = translator.translate(resultCode);
            String resultMsg = resultMap.get("resultMsg");
            String errorBody = String.format("[%s-%s]", resultCode.getCode(), resultMsg);
            logDO.setResponseBody(errorBody);

            httpInterfaceCallLogDao.insert(logDO);
        } catch (Exception e) {
            logger.warn("Failed to record error call log", e);
        }
    }

    private String buildRespTitle(ResultCode resultCode) {
        Map<String, String> resultMap = translator.translate(resultCode);
        String resultMsg = resultMap.get("resultMsg");
        return String.format("[%s-%s]", resultCode.getCode(), resultMsg);
    }

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResponseEntity<String> handleBizException(BizException e, HttpServletRequest request, HttpServletResponse response) {
        ResultCode resultCode = e.getResultCode();
        logger.warn("", e);
        
        // 记录错误日志
        recordErrorCallLog(request, response, resultCode);
        
        if (e.getResultCode() == ResultCode.GROOVY_COMPILE_EXCEPTION || e.getResultCode() == ResultCode.GROOVY_RUNTIME_EXCEPTION) {
            String body = buildRespTitle(resultCode) + "\n"
                       + "###############################################################################\n"
                       + e.getCause().getMessage() + "\n"
                       + "##############################################################################\n";
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(buildRespTitle(resultCode), HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        logger.error("", e);
        
        // 记录错误日志
        recordErrorCallLog(request, response, ResultCode.UNEXPECTED_ERROR);
        
        return new ResponseEntity<>(buildRespTitle(ResultCode.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

