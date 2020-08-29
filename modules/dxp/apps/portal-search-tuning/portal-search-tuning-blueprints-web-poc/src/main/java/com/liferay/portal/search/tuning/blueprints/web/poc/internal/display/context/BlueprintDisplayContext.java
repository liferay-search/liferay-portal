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

package com.liferay.portal.search.tuning.blueprints.web.poc.internal.display.context;

import java.util.Map;

/**
 * @author Kevin Tan
 */
public class BlueprintDisplayContext {

	public Map<String, Object> getData() {
		return _data;
	}

	public void setData(Map<String, Object> data) {
		_data = data;
	}

	private Map<String, Object> _data;

}