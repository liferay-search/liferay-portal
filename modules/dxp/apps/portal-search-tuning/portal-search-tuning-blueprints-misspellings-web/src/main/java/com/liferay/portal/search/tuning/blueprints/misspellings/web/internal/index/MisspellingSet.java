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

import java.util.Date;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class MisspellingSet {

	public MisspellingSet(MisspellingSet misspellings) {
		_companyId = misspellings._companyId;
		_created = misspellings._created;
		_groupId = misspellings._groupId;
		_languageId = misspellings._languageId;
		_misspellings = misspellings._misspellings;
		_misspellingSetId = misspellings._misspellingSetId;
		_modified = misspellings._modified;
		_name = misspellings._name;
		_phrase = misspellings._phrase;
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

	public String getLanguageId() {
		return _languageId;
	}

	public List<String> getMisspellings() {
		return _misspellings;
	}

	public String getMisspellingSetId() {
		return _misspellingSetId;
	}

	public Date getModified() {
		return _modified;
	}

	public String getName() {
		return _name;
	}

	public String getPhrase() {
		return _phrase;
	}

	public long getUserId() {
		return _userId;
	}

	public static class MisspellingSetBuilder {

		public MisspellingSetBuilder() {
			_misspellingSet = new MisspellingSet();
		}

		public MisspellingSetBuilder(
			MisspellingSet MisspellingSet) {

			_misspellingSet = MisspellingSet;
		}

		public MisspellingSet build() {
			return new MisspellingSet(_misspellingSet);
		}

		public MisspellingSetBuilder companyId(long companyId) {
			_misspellingSet._companyId = companyId;

			return this;
		}

		public MisspellingSetBuilder created(Date created) {
			_misspellingSet._created = created;

			return this;
		}

		public MisspellingSetBuilder groupId(long groupId) {
			_misspellingSet._groupId = groupId;

			return this;
		}

		public MisspellingSetBuilder languageId(String languageId) {
			_misspellingSet._languageId = languageId;

			return this;
		}

		public MisspellingSetBuilder misspellings(List<String> misspellings) {
			_misspellingSet._misspellings = misspellings;

			return this;
		}

		public MisspellingSetBuilder misspellingSetId(
			String MisspellingSetId) {

			_misspellingSet._misspellingSetId =
				MisspellingSetId;

			return this;
		}

		public MisspellingSetBuilder modified(Date modified) {
			_misspellingSet._modified = modified;

			return this;
		}

		public MisspellingSetBuilder name(String name) {
			_misspellingSet._name = name;

			return this;
		}

		public MisspellingSetBuilder phrase(String phrase) {
			_misspellingSet._phrase = phrase;

			return this;
		}

		public MisspellingSetBuilder userId(long userId) {
			_misspellingSet._userId = userId;

			return this;
		}

		private final MisspellingSet _misspellingSet;

	}

	private MisspellingSet() {
	}

	private long _companyId;
	private Date _created;
	private long _groupId;
	private String _languageId;
	private List<String> _misspellings;
	private String _misspellingSetId;
	private Date _modified;
	private String _name;
	private String _phrase;
	private long _userId;

}