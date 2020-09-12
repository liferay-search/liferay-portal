package com.liferay.portal.search.elasticsearch7.internal.proxy;

import com.liferay.portal.kernel.messaging.proxy.BaseMultiDestinationProxyBean;
import com.liferay.portal.kernel.messaging.proxy.ProxyModeThreadLocal;
import com.liferay.portal.kernel.messaging.proxy.ProxyRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author André de Oliveira
 * @author Shuyang Zhou
 */
public class SearchMultiDestinationMessagingProxyInvocationHandler
	implements InvocationHandler {

	public SearchMultiDestinationMessagingProxyInvocationHandler(
		BaseMultiDestinationProxyBean baseMultiDestinationProxyBean) {

		_baseMultiDestinationProxyBean = baseMultiDestinationProxyBean;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable {

		ProxyRequest proxyRequest = new ProxyRequest(method, args);

		boolean synchronous = proxyRequest.isSynchronous();

		if (false) {
			if (ProxyModeThreadLocal.isForceSync()) {
				synchronous = true;
			}
		}

		if (synchronous) {
			return _baseMultiDestinationProxyBean.synchronousSend(proxyRequest);
		}

		_baseMultiDestinationProxyBean.send(proxyRequest);

		return null;
	}

	private final BaseMultiDestinationProxyBean _baseMultiDestinationProxyBean;

}