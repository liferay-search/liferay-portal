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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index;

import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.constants.QueryStringStatus;

import java.util.Date;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class QueryString {

	public QueryString(QueryString queryString) {
		_companyId = queryString._companyId;
		_content = queryString._content;
		_created = queryString._created;
		_groupId = queryString._groupId;
		_hitCount = queryString._hitCount;
		_languageId = queryString._languageId;
		_lastAccessed = queryString._lastAccessed;
		_lastReported = queryString._lastReported;
		_modified = queryString._modified;
		_queryStringId = queryString._queryStringId;
		_reportCount = queryString._reportCount;
		_reports = queryString._reports;
		_status = queryString._status;
		_statusDate = queryString._statusDate;
	}

	public Long getCompanyId() {
		return _companyId;
	}

	public String getContent() {
		return _content;
	}

	public Date getCreated() {
		return _created;
	}

	public Long getGroupId() {
		return _groupId;
	}

	public Long getHitCount() {
		return _hitCount;
	}

	public String getLanguageId() {
		return _languageId;
	}

	public Date getLastAccessed() {
		return _lastAccessed;
	}

	public Date getLastReported() {
		return _lastReported;
	}

	public Date getModified() {
		return _modified;
	}

	public String getQueryStringId() {
		return _queryStringId;
	}

	public Long getReportCount() {
		return _reportCount;
	}

	public List<String> getReports() {
		return _reports;
	}

	public QueryStringStatus getStatus() {
		return _status;
	}

	public Date getStatusDate() {
		return _statusDate;
	}

	public static class QueryStringBuilder {

		public QueryStringBuilder() {
			_queryString = new QueryString();
		}

		public QueryStringBuilder(QueryString queryString) {
			_queryString = queryString;
		}

		public QueryString build() {
			return new QueryString(_queryString);
		}

		public QueryStringBuilder companyId(Long companyId) {
			_queryString._companyId = companyId;

			return this;
		}

		public QueryStringBuilder content(String content) {
			_queryString._content = content;

			return this;
		}

		public QueryStringBuilder created(Date created) {
			_queryString._created = created;

			return this;
		}

		public QueryStringBuilder groupId(Long groupId) {
			_queryString._groupId = groupId;

			return this;
		}

		public QueryStringBuilder hitCount(Long hitCount) {
			_queryString._hitCount = hitCount;

			return this;
		}

		public QueryStringBuilder languageId(String languageId) {
			_queryString._languageId = languageId;

			return this;
		}

		public QueryStringBuilder lastAccessed(Date lastAccessed) {
			_queryString._lastAccessed = lastAccessed;

			return this;
		}

		public QueryStringBuilder lastReported(Date lastReported) {
			_queryString._lastReported = lastReported;

			return this;
		}

		public QueryStringBuilder modified(Date modified) {
			_queryString._modified = modified;

			return this;
		}

		public QueryStringBuilder queryStringId(String queryStringId) {
			_queryString._queryStringId = queryStringId;

			return this;
		}

		public QueryStringBuilder reportCount(List<String> reports) {
			_queryString._reports = reports;

			return this;
		}

		public QueryStringBuilder reportCount(Long reportCount) {
			_queryString._reportCount = reportCount;

			return this;
		}

		public QueryStringBuilder status(QueryStringStatus status) {
			_queryString._status = status;

			return this;
		}

		public QueryStringBuilder statusDate(Date statusDate) {
			_queryString._statusDate = statusDate;

			return this;
		}

		private final QueryString _queryString;

	}

	private QueryString() {
	}

	private Long _companyId;
	private String _content;
	private Date _created;
	private Long _groupId;
	private Long _hitCount;
	private String _languageId;
	private Date _lastAccessed;
	private Date _lastReported;
	private Date _modified;
	private String _queryStringId;
	private Long _reportCount;
	private List<String> _reports;
	private QueryStringStatus _status;
	private Date _statusDate;

}