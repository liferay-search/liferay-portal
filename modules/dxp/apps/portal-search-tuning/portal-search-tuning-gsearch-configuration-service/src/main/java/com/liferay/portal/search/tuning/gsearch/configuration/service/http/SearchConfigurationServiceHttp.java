/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.search.tuning.gsearch.configuration.service.http;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.HttpPrincipal;
import com.liferay.portal.kernel.service.http.TunnelUtil;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationServiceUtil;

/**
 * Provides the HTTP utility for the
 * <code>SearchConfigurationServiceUtil</code> service
 * utility. The
 * static methods of this class calls the same methods of the service utility.
 * However, the signatures are different because it requires an additional
 * <code>HttpPrincipal</code> parameter.
 *
 * <p>
 * The benefits of using the HTTP utility is that it is fast and allows for
 * tunneling without the cost of serializing to text. The drawback is that it
 * only works with Java.
 * </p>
 *
 * <p>
 * Set the property <b>tunnel.servlet.hosts.allowed</b> in portal.properties to
 * configure security.
 * </p>
 *
 * <p>
 * The HTTP utility is only generated for remote services.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see SearchConfigurationServiceSoap
 * @generated
 */
public class SearchConfigurationServiceHttp {

	public static com.liferay.portal.search.tuning.gsearch.configuration.model.
		SearchConfiguration addConfiguration(
				HttpPrincipal httpPrincipal,
				java.util.Map<java.util.Locale, String> titleMap,
				java.util.Map<java.util.Locale, String> descriptionMap,
				String configuration, int type,
				com.liferay.portal.kernel.service.ServiceContext serviceContext)
			throws com.liferay.portal.kernel.exception.PortalException {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class, "addConfiguration",
				_addConfigurationParameterTypes0);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, titleMap, descriptionMap, configuration, type,
				serviceContext);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				if (exception instanceof
						com.liferay.portal.kernel.exception.PortalException) {

					throw (com.liferay.portal.kernel.exception.PortalException)
						exception;
				}

				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (com.liferay.portal.search.tuning.gsearch.configuration.
				model.SearchConfiguration)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static com.liferay.portal.search.tuning.gsearch.configuration.model.
		SearchConfiguration deleteConfiguration(
				HttpPrincipal httpPrincipal, long searchConfigurationId)
			throws com.liferay.portal.kernel.exception.PortalException {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class, "deleteConfiguration",
				_deleteConfigurationParameterTypes1);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, searchConfigurationId);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				if (exception instanceof
						com.liferay.portal.kernel.exception.PortalException) {

					throw (com.liferay.portal.kernel.exception.PortalException)
						exception;
				}

				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (com.liferay.portal.search.tuning.gsearch.configuration.
				model.SearchConfiguration)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static com.liferay.portal.search.tuning.gsearch.configuration.model.
		SearchConfiguration getConfiguration(
				HttpPrincipal httpPrincipal, long searchConfigurationId)
			throws com.liferay.portal.kernel.exception.PortalException {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class, "getConfiguration",
				_getConfigurationParameterTypes2);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, searchConfigurationId);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				if (exception instanceof
						com.liferay.portal.kernel.exception.PortalException) {

					throw (com.liferay.portal.kernel.exception.PortalException)
						exception;
				}

				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (com.liferay.portal.search.tuning.gsearch.configuration.
				model.SearchConfiguration)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static java.util.List
		<com.liferay.portal.search.tuning.gsearch.configuration.model.
			SearchConfiguration> getGroupConfigurations(
				HttpPrincipal httpPrincipal, long groupId, int type, int start,
				int end) {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class, "getGroupConfigurations",
				_getGroupConfigurationsParameterTypes3);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, groupId, type, start, end);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (java.util.List
				<com.liferay.portal.search.tuning.gsearch.configuration.model.
					SearchConfiguration>)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static java.util.List
		<com.liferay.portal.search.tuning.gsearch.configuration.model.
			SearchConfiguration> getGroupConfigurations(
				HttpPrincipal httpPrincipal, long groupId, int status, int type,
				int start, int end) {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class, "getGroupConfigurations",
				_getGroupConfigurationsParameterTypes4);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, groupId, status, type, start, end);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (java.util.List
				<com.liferay.portal.search.tuning.gsearch.configuration.model.
					SearchConfiguration>)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static java.util.List
		<com.liferay.portal.search.tuning.gsearch.configuration.model.
			SearchConfiguration> getGroupConfigurations(
				HttpPrincipal httpPrincipal, long groupId, int status, int type,
				int start, int end,
				com.liferay.portal.kernel.util.OrderByComparator
					<com.liferay.portal.search.tuning.gsearch.configuration.
						model.SearchConfiguration> orderByComparator) {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class, "getGroupConfigurations",
				_getGroupConfigurationsParameterTypes5);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, groupId, status, type, start, end,
				orderByComparator);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (java.util.List
				<com.liferay.portal.search.tuning.gsearch.configuration.model.
					SearchConfiguration>)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static java.util.List
		<com.liferay.portal.search.tuning.gsearch.configuration.model.
			SearchConfiguration> getGroupConfigurations(
				HttpPrincipal httpPrincipal, long groupId, int type, int start,
				int end,
				com.liferay.portal.kernel.util.OrderByComparator
					<com.liferay.portal.search.tuning.gsearch.configuration.
						model.SearchConfiguration> orderByComparator) {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class, "getGroupConfigurations",
				_getGroupConfigurationsParameterTypes6);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, groupId, type, start, end, orderByComparator);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (java.util.List
				<com.liferay.portal.search.tuning.gsearch.configuration.model.
					SearchConfiguration>)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static int getGroupConfigurationsCount(
		HttpPrincipal httpPrincipal, long groupId, int type) {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class,
				"getGroupConfigurationsCount",
				_getGroupConfigurationsCountParameterTypes7);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, groupId, type);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return ((Integer)returnObj).intValue();
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static int getGroupConfigurationsCount(
		HttpPrincipal httpPrincipal, long groupId, int status, int type) {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class,
				"getGroupConfigurationsCount",
				_getGroupConfigurationsCountParameterTypes8);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, groupId, status, type);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return ((Integer)returnObj).intValue();
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	public static com.liferay.portal.search.tuning.gsearch.configuration.model.
		SearchConfiguration updateConfiguration(
				HttpPrincipal httpPrincipal, long searchConfigurationId,
				java.util.Map<java.util.Locale, String> titleMap,
				java.util.Map<java.util.Locale, String> descriptionMap,
				String configuration,
				com.liferay.portal.kernel.service.ServiceContext serviceContext)
			throws com.liferay.portal.kernel.exception.PortalException {

		try {
			MethodKey methodKey = new MethodKey(
				SearchConfigurationServiceUtil.class, "updateConfiguration",
				_updateConfigurationParameterTypes9);

			MethodHandler methodHandler = new MethodHandler(
				methodKey, searchConfigurationId, titleMap, descriptionMap,
				configuration, serviceContext);

			Object returnObj = null;

			try {
				returnObj = TunnelUtil.invoke(httpPrincipal, methodHandler);
			}
			catch (Exception exception) {
				if (exception instanceof
						com.liferay.portal.kernel.exception.PortalException) {

					throw (com.liferay.portal.kernel.exception.PortalException)
						exception;
				}

				throw new com.liferay.portal.kernel.exception.SystemException(
					exception);
			}

			return (com.liferay.portal.search.tuning.gsearch.configuration.
				model.SearchConfiguration)returnObj;
		}
		catch (com.liferay.portal.kernel.exception.SystemException
					systemException) {

			_log.error(systemException, systemException);

			throw systemException;
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		SearchConfigurationServiceHttp.class);

	private static final Class<?>[] _addConfigurationParameterTypes0 =
		new Class[] {
			java.util.Map.class, java.util.Map.class, String.class, int.class,
			com.liferay.portal.kernel.service.ServiceContext.class
		};
	private static final Class<?>[] _deleteConfigurationParameterTypes1 =
		new Class[] {long.class};
	private static final Class<?>[] _getConfigurationParameterTypes2 =
		new Class[] {long.class};
	private static final Class<?>[] _getGroupConfigurationsParameterTypes3 =
		new Class[] {long.class, int.class, int.class, int.class};
	private static final Class<?>[] _getGroupConfigurationsParameterTypes4 =
		new Class[] {long.class, int.class, int.class, int.class, int.class};
	private static final Class<?>[] _getGroupConfigurationsParameterTypes5 =
		new Class[] {
			long.class, int.class, int.class, int.class, int.class,
			com.liferay.portal.kernel.util.OrderByComparator.class
		};
	private static final Class<?>[] _getGroupConfigurationsParameterTypes6 =
		new Class[] {
			long.class, int.class, int.class, int.class,
			com.liferay.portal.kernel.util.OrderByComparator.class
		};
	private static final Class<?>[]
		_getGroupConfigurationsCountParameterTypes7 = new Class[] {
			long.class, int.class
		};
	private static final Class<?>[]
		_getGroupConfigurationsCountParameterTypes8 = new Class[] {
			long.class, int.class, int.class
		};
	private static final Class<?>[] _updateConfigurationParameterTypes9 =
		new Class[] {
			long.class, java.util.Map.class, java.util.Map.class, String.class,
			com.liferay.portal.kernel.service.ServiceContext.class
		};

}