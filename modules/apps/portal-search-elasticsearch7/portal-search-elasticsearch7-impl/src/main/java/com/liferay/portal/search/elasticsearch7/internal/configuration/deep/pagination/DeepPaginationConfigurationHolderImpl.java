/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.elasticsearch7.internal.configuration.deep.pagination;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.search.elasticsearch7.configuration.DeepPaginationConfiguration;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Gustavo Lima
 */
@Component(
	configurationPid = "com.liferay.portal.search.elasticsearch7.configuration.DeepPaginationConfiguration",
	service = DeepPaginationConfigurationHolder.class
)
public class DeepPaginationConfigurationHolderImpl
	implements DeepPaginationConfigurationHolder {

	@Override
	public boolean getEnableDeepPagination() {
		return _enableDeepPagination;
	}

	@Override
	public String getPointInTimeKeepAlive() {
		return _pointInTimeKeepAlive;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		DeepPaginationConfiguration deepPaginationConfiguration =
			ConfigurableUtil.createConfigurable(
				DeepPaginationConfiguration.class, properties);

		_enableDeepPagination =
			deepPaginationConfiguration.enableDeepPagination();
		_pointInTimeKeepAlive = _validatePointInTimeString(
			deepPaginationConfiguration.pointInTimeKeepAlive());
	}

	private String _validatePointInTimeString(String pointInTimeKeepAlive) {
		if (!pointInTimeKeepAlive.matches(
				"^\\d+(?:d|h|m|s|ms|micros|nanos)$")) {

			return "5m";
		}

		return pointInTimeKeepAlive;
	}

	private volatile boolean _enableDeepPagination;
	private volatile String _pointInTimeKeepAlive;

}