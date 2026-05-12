/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.preview;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.kernel.aop.AopMethodInvocation;
import com.liferay.portal.kernel.aop.ChainableMethodAdvice;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.preview.Previewable;
import com.liferay.portal.kernel.service.PersistedModelLocalService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Shuyang Zhou
 */
public class PreviewableAdvice extends ChainableMethodAdvice {

	@Override
	public Object createMethodContext(
		Class<?> targetClass, Method method,
		Map<Class<? extends Annotation>, Annotation> annotations) {

		Previewable previewable = (Previewable)annotations.get(
			Previewable.class);

		if ((previewable == null) || !previewable.enabled() ||
			!PersistedModelLocalService.class.isAssignableFrom(targetClass)) {

			return null;
		}

		Class<?> modelClass = _getModelClass(targetClass);

		if ((modelClass == targetClass) || !_isSupported(method.getName())) {
			return null;
		}

		Type type = method.getGenericReturnType();

		if (modelClass == type) {
			return _baseModelResolver;
		}

		if ((type instanceof ParameterizedType parameterizedType) &&
			(modelClass == parameterizedType.getActualTypeArguments()[0])) {

			Class<?> rawType = (Class<?>)parameterizedType.getRawType();

			if (Collection.class.isAssignableFrom(rawType)) {
				if (rawType == List.class) {
					return _listBaseModelResolver;
				}

				if (rawType == Set.class) {
					return _setBaseModelResolver;
				}

				if (rawType == Queue.class) {
					return _queueBaseModelResolver;
				}
			}
		}

		return null;
	}

	@Override
	protected Object afterReturning(
			AopMethodInvocation aopMethodInvocation, Object[] arguments,
			Object result)
		throws Throwable {

		if (result == null) {
			return null;
		}

		Resolver resolver = aopMethodInvocation.getAdviceMethodContext();

		return resolver.resolve(result);
	}

	private Method _findGetModelClassMethod(Class<?> clazz) {
		while (clazz != Object.class) {
			Method method = ReflectionUtil.fetchDeclaredMethod(
				true, clazz, "getModelClass");

			if (method != null) {
				return method;
			}

			clazz = clazz.getSuperclass();
		}

		return null;
	}

	private Class<?> _getModelClass(Class<?> targetClass) {
		return _modelClasses.computeIfAbsent(
			targetClass,
			key -> {
				try {
					Method method = _findGetModelClassMethod(key);

					if (method == null) {
						return key;
					}

					Constructor<?> constructor = key.getDeclaredConstructor();

					return (Class<?>)method.invoke(constructor.newInstance());
				}
				catch (ReflectiveOperationException
							reflectiveOperationException) {

					_log.error(reflectiveOperationException);

					return key;
				}
			});
	}

	private boolean _isSupported(String name) {
		boolean supported = false;

		for (String supportedMethodNamePrefix : _supportedMethodNamePrefixes) {
			if (name.startsWith(supportedMethodNamePrefix)) {
				supported = true;

				break;
			}
		}

		return supported;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PreviewableAdvice.class);

	private static final Resolver _baseModelResolver =
		obj -> PreviewableResolverUtil.resolve((BaseModel<?>)obj);
	private static final Resolver _listBaseModelResolver =
		list -> PreviewableResolverUtil.resolve(
			(Collection<BaseModel<?>>)list, new ArrayList<>());
	private static final Map<Class<?>, Class<?>> _modelClasses =
		new ConcurrentHashMap<>();
	private static final Resolver _queueBaseModelResolver =
		queue -> PreviewableResolverUtil.resolve(
			(Collection<BaseModel<?>>)queue, new ArrayDeque<>());
	private static final Resolver _setBaseModelResolver =
		set -> PreviewableResolverUtil.resolve(
			(Collection<BaseModel<?>>)set, new HashSet<>());
	private static final Set<String> _supportedMethodNamePrefixes = Set.of(
		"dslQuery", "dynamicQuery", "fetch", "get", "load", "search");

	private interface Resolver {

		public Object resolve(Object object);

	}

}