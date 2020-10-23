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

import com.liferay.portal.kernel.json.JSONObject;

import java.util.Date;

/**
 * @author Petteri Karttunen
 */
public class MisspellingsDefinition {

	public MisspellingsDefinition(MisspellingsDefinition misspellings) {
		_companyId = misspellings._companyId;
		_created = misspellings._created;
		_groupId = misspellings._groupId;
		_mappingsJsonObject = misspellings._mappingsJsonObject;
		_misspellingsDefinitionId = misspellings._misspellingsDefinitionId;
		_modified = misspellings._modified;
		_name = misspellings._name;
		_userId = misspellings._userId;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public Date getCreated() {
		return _created;
	}

	public long getGroupId() {
		return _groupId;
	}

	public JSONObject getMappingsJSONObject() {
		return _mappingsJsonObject;
	}

	public String getMisspellingsDefinitionId() {
		return _misspellingsDefinitionId;
	}

	public Date getModified() {
		return _modified;
	}

	public String getName() {
		return _name;
	}

	public long getUserId() {
		return _userId;
	}

	public static class MisspellingsBuilder {

		public MisspellingsBuilder() {
			_misspellingsDefinition = new MisspellingsDefinition();
		}

		public MisspellingsBuilder(
			MisspellingsDefinition misspellingsDefinition) {

			_misspellingsDefinition = misspellingsDefinition;
		}

		public MisspellingsDefinition build() {
			return new MisspellingsDefinition(_misspellingsDefinition);
		}

		public MisspellingsBuilder companyId(long companyId) {
			_misspellingsDefinition._companyId = companyId;

			return this;
		}

		public MisspellingsBuilder created(Date created) {
			_misspellingsDefinition._created = created;

			return this;
		}

		public MisspellingsBuilder groupId(long groupId) {
			_misspellingsDefinition._groupId = groupId;

			return this;
		}

		public MisspellingsBuilder mappings(JSONObject mappingsJsonObject) {
			_misspellingsDefinition._mappingsJsonObject = mappingsJsonObject;

			return this;
		}

		public MisspellingsBuilder misspellingsDefinitionId(
			String misspellingsDefinitionId) {

			_misspellingsDefinition._misspellingsDefinitionId =
				misspellingsDefinitionId;

			return this;
		}

		public MisspellingsBuilder modified(Date modified) {
			_misspellingsDefinition._modified = modified;

			return this;
		}

		public MisspellingsBuilder name(String name) {
			_misspellingsDefinition._name = name;

			return this;
		}

		public MisspellingsBuilder userId(long userId) {
			_misspellingsDefinition._userId = userId;

			return this;
		}

		private final MisspellingsDefinition _misspellingsDefinition;

	}

	private MisspellingsDefinition() {
	}

	private long _companyId;
	private Date _created;
	private long _groupId;
	private JSONObject _mappingsJsonObject;
	private String _misspellingsDefinitionId;
	private Date _modified;
	private String _name;
	private long _userId;

}