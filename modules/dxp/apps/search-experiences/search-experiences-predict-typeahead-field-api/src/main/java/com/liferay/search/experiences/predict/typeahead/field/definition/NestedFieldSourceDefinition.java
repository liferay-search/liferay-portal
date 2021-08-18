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

import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.Operator;
import com.liferay.portal.search.query.Query;

import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class NestedFieldSourceDefinition
	implements FieldTypeaheadSourceDefinition {

	public NestedFieldSourceDefinition(
		NestedFieldSourceDefinition nestedFieldSourceDefinition) {

		_nestedMustTermMap = nestedFieldSourceDefinition._nestedMustTermMap;
		_path = nestedFieldSourceDefinition._path;
		_termFilterMap = nestedFieldSourceDefinition._termFilterMap;
		_valueFieldName = nestedFieldSourceDefinition._valueFieldName;
	}

	@Override
	public Query accept(
		FieldTypeaheadSourceDefinitionVisitor
			fieldTypeaheadSourceDefinitionVisitor,
		String fuzziness, Operator operator, int prefixLength) {

		return fieldTypeaheadSourceDefinitionVisitor.visit(
			this, fuzziness, operator, prefixLength);
	}

	public Map<String, String> getNestedMustTermMap() {
		return _nestedMustTermMap;
	}

	public String getPath() {
		return _path;
	}

	@Override
	public Map<String, String> getTermFilterMap() {
		return _termFilterMap;
	}

	public String getValueFieldName() {
		return _valueFieldName;
	}

	public static class NestedFieldSourceDefinitionBuilder {

		public NestedFieldSourceDefinitionBuilder() {
			_nestedFieldSourceDefinition = new NestedFieldSourceDefinition();
		}

		public NestedFieldSourceDefinitionBuilder(
			NestedFieldSourceDefinition nestedFieldSourceDefinition) {

			_nestedFieldSourceDefinition = nestedFieldSourceDefinition;
		}

		public NestedFieldSourceDefinition build() {
			NestedFieldSourceDefinition nestedFieldSourceDefinition =
				new NestedFieldSourceDefinition(_nestedFieldSourceDefinition);

			_validateCriteria(nestedFieldSourceDefinition);

			return nestedFieldSourceDefinition;
		}

		public NestedFieldSourceDefinitionBuilder nestedMustTermMap(
			Map<String, String> nestedMustTermMap) {

			_nestedFieldSourceDefinition._nestedMustTermMap = nestedMustTermMap;

			return this;
		}

		public NestedFieldSourceDefinitionBuilder path(String path) {
			_nestedFieldSourceDefinition._path = path;

			return this;
		}

		public NestedFieldSourceDefinitionBuilder termFilterMap(
			Map<String, String> termFilterMap) {

			_nestedFieldSourceDefinition._termFilterMap = termFilterMap;

			return this;
		}

		public NestedFieldSourceDefinitionBuilder valueFieldName(
			String valueFieldName) {

			_nestedFieldSourceDefinition._valueFieldName = valueFieldName;

			return this;
		}

		private void _validateCriteria(
			NestedFieldSourceDefinition nestedFieldSourceDefinition) {

			if (Validator.isBlank(nestedFieldSourceDefinition.getPath()) ||
				Validator.isBlank(
					nestedFieldSourceDefinition.getValueFieldName())) {

				throw new IllegalStateException(
					"Path and value field names are mandatory attributes");
			}
		}

		private final NestedFieldSourceDefinition _nestedFieldSourceDefinition;

	}

	private NestedFieldSourceDefinition() {
	}

	private Map<String, String> _nestedMustTermMap;
	private String _path;
	private Map<String, String> _termFilterMap;
	private String _valueFieldName;

}