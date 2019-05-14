package com.twitter.auth;


import com.twitter.util.constant.HttpRequestConsts;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * 注入uid参数
 * @author Jam
 */
public class CurrentUidMethodArgumentResolver implements
        HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter param) {
		if (!param.getParameterType().isAssignableFrom(Integer.class)) return false;
		
		return param.hasParameterAnnotation(CurrentUid.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
		Integer uid = (Integer) webRequest.getAttribute(HttpRequestConsts.ATTR_CURRENT_UID, RequestAttributes.SCOPE_REQUEST);
		if (uid == null) {
			throw new MissingServletRequestPartException(HttpRequestConsts.ATTR_CURRENT_UID);
		}
		
		return uid;
	}
}
