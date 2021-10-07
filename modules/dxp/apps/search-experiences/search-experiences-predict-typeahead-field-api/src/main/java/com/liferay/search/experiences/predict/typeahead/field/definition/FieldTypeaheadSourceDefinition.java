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

package com.liferay.search.experiences.predict.typeahead.field.definition;

import com.liferay.portal.search.query.Operator;
import com.liferay.portal.search.query.Query;

import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public interface FieldTypeaheadSourceDefinition {

	public Query accept(
		FieldTypeaheadSourceDefinitionVisitor
			fieldTypeaheadSourceDefinitionVisitor,
		String fuzziness, Operator operator, int prefixLength);

	public Map<String, String> getTermFilterMap();

}