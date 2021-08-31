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

package com.liferay.search.experiences.blueprints.facets.internal.util;

import com.liferay.portal.kernel.json.JSONObject;

import java.util.Iterator;
import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public class BlueprintJSONUtil {

	public static Optional<String> getFirstKeyOptional(JSONObject jsonObject) {
		if (jsonObject == null) {
			return Optional.empty();
		}

		Iterator<String> iterator = jsonObject.keys();

		if (iterator.hasNext()) {
			return Optional.of(iterator.next());
		}

		return Optional.empty();
	}

}