package com.twitter.auth;


import com.twitter.service.TokenService;
import com.twitter.util.constant.HttpRequestConsts;
import com.twitter.util.controller.AbstractController;
import com.twitter.util.controller.CommonResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 判断请求是否有权限
 * 
 * @author Jam
 */
@Component
public class AuthorizeInterceptor extends HandlerInterceptorAdapter {
	
	@Autowired
	TokenService tokenService;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		Method method = handlerMethod.getMethod();
		boolean isOa = false;
		if (method.getAnnotation(Authorize.class) == null) {
			return true;
		}

		String token = request.getHeader(HttpRequestConsts.HEADER_TOKEN);
		//如果上述地方获取的token都为空则查看oa的token是否为空
		/*if(StringUtils.isEmpty(token)){
			token = request.getHeader(HttpRequestConsts.HEADER_OA_TOKEN);
			if(!StringUtils.isEmpty(token)){
				isOa = true;
			}
		}
		if(!isOa) {*/
			Integer uid = this.tokenService.getUid(token);
			if (uid == null) {
				returnJsonResponse(response);
				return false;
			}
			request.setAttribute(HttpRequestConsts.ATTR_CURRENT_UID, uid);
		/*}else{
			Integer uid = this.tokenService.getOaUid(token);
			if (uid == null) {
				returnJsonResponse(response);
				return false;
			}
			request.setAttribute(HttpRequestConsts.OA_CURRENT_UID, uid);
		}*/

		return true;
	}

	private void returnJsonResponse(HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append(AbstractController.buildFailed(CommonResultCode.REQUEST_NO_AUTH).toString());
	}

}
