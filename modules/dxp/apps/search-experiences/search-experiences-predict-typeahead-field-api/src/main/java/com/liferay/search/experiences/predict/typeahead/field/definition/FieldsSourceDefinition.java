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
public class FieldsSourceDefinition implements FieldTypeaheadSourceDefinition {

	public FieldsSourceDefinition(
		FieldsSourceDefinition fieldsSourceDefinition) {

		_fieldsBoosts = fieldsSourceDefinition._fieldsBoosts;
		_termFilterMap = fieldsSourceDefinition._termFilterMap;
	}

	@Override
	public Query accept(
		FieldTypeaheadSourceDefinitionVisitor
			fieldTypeaheadSourceDefinitionVisitor,
		String fuzziness, Operator operator, int prefixLength) {

		return fieldTypeaheadSourceDefinitionVisitor.visit(
			this, fuzziness, operator, prefixLength);
	}

	public Map<String, Float> getFieldsBoosts() {
		return _fieldsBoosts;
	}

	@Override
	public Map<String, String> getTermFilterMap() {
		return _termFilterMap;
	}

	public static class FieldsSourceDefinitionBuilder {

		public FieldsSourceDefinitionBuilder() {
			_fieldsSourceDefinition = new FieldsSourceDefinition();
		}

		public FieldsSourceDefinitionBuilder(
			FieldsSourceDefinition fieldsSourceDefinition) {

			_fieldsSourceDefinition = fieldsSourceDefinition;
		}

		public FieldsSourceDefinition build() {
			FieldsSourceDefinition fieldsSourceDefinition =
				new FieldsSourceDefinition(_fieldsSourceDefinition);

			_validateCriteria(fieldsSourceDefinition);

			return fieldsSourceDefinition;
		}

		public FieldsSourceDefinitionBuilder fieldsBoosts(
			Map<String, Float> fieldsBoosts) {

			_fieldsSourceDefinition._fieldsBoosts = fieldsBoosts;

			return this;
		}

		public FieldsSourceDefinitionBuilder termFilterMap(
			Map<String, String> termFilterMap) {

			_fieldsSourceDefinition._termFilterMap = termFilterMap;

			return this;
		}

		private void _validateCriteria(
			FieldsSourceDefinition fieldsSourceDefinition) {

			if (fieldsSourceDefinition.getFieldsBoosts() == null) {
				throw new IllegalStateException(
					"Fieldsboosts is mandatory attribute");
			}
		}

		private final FieldsSourceDefinition _fieldsSourceDefinition;

	}

	private FieldsSourceDefinition() {
	}

	private Map<String, Float> _fieldsBoosts;
	private Map<String, String> _termFilterMap;

}