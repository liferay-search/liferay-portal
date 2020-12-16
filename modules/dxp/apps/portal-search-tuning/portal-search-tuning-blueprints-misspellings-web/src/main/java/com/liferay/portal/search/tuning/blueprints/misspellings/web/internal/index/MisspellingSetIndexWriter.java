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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index;

import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexName;

/**
 * @author Petteri Karttunen
 */
public interface MisspellingSetIndexWriter {

	public String create(
		MisspellingSetIndexName misspellingsIndexName,
		MisspellingSet misspellingSet);

	public void remove(
		MisspellingSetIndexName misspellingsIndexName, String id);

	public void update(
		MisspellingSetIndexName misspellingsIndexName,
		MisspellingSet misspellingSet);

}