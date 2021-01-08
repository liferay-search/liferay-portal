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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.util;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.search.tuning.blueprints.query.index.configuration.QueryIndexConfiguration;
import com.liferay.portal.search.tuning.blueprints.query.index.constants.QueryStringStatus;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.util.QueryIndexHelper;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexReader;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexWriter;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	configurationPid = "com.liferay.portal.search.tuning.blueprints.query.index.configuration.QueryIndexConfiguration",
	immediate = true, service = QueryIndexHelper.class
)
public class QueryIndexHelperImpl implements QueryIndexHelper {

	@Override
	public void indexKeywords(
		long companyId, long groupId, String languageId, String keywords) {

		if (!_queryIndexConfiguration.enable()) {
			return;
		}

		Optional<String> queryStringIdOptional =
			_queryStringIndexReader.fetchIdOptional(
				_queryStringIndexNameBuilder.getQueryStringIndexName(companyId),
				companyId, groupId, keywords);

		if (queryStringIdOptional.isPresent()) {
			_updateHitcount(companyId, queryStringIdOptional.get());
		}
		else {
			_addQueryString(companyId, groupId, languageId, keywords);
		}
	}

	public boolean isAnalyzedLanguage(String languageId) {
		return QueryIndexUtil.isAnalyzedLanguage(languageId);
	}

	@Override
	public void reportKeywords(long companyId, long groupId, String keywords) {
		Optional<String> queryStringIdOptional =
			_queryStringIndexReader.fetchIdOptional(
				_queryStringIndexNameBuilder.getQueryStringIndexName(companyId),
				companyId, groupId, keywords);

		if (queryStringIdOptional.isPresent()) {
			_queryStringIndexWriter.addReport(
				_queryStringIndexNameBuilder.getQueryStringIndexName(companyId),
				queryStringIdOptional.get());
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_queryIndexConfiguration = ConfigurableUtil.createConfigurable(
			QueryIndexConfiguration.class, properties);
	}

	private void _addQueryString(
		long companyId, long groupId, String languageId, String keywords) {

		_queryStringIndexWriter.create(
			_queryStringIndexNameBuilder.getQueryStringIndexName(companyId),
			new QueryString.QueryStringBuilder().companyId(
				companyId
			).content(
				keywords
			).created(
				new Date()
			).groupId(
				groupId
			).hitCount(
				1L
			).languageId(
				languageId
			).lastAccessed(
				new Date()
			).modified(
				new Date()
			).status(
				QueryStringStatus.ACTIVE
			).statusDate(
				new Date()
			).build());
	}

	private void _updateHitcount(long companyId, String id) {
		_queryStringIndexWriter.addHit(
			_queryStringIndexNameBuilder.getQueryStringIndexName(companyId),
			id);
	}

	private volatile QueryIndexConfiguration _queryIndexConfiguration;

	@Reference
	private QueryStringIndexNameBuilder _queryStringIndexNameBuilder;

	@Reference
	private QueryStringIndexReader _queryStringIndexReader;

	@Reference
	private QueryStringIndexWriter _queryStringIndexWriter;

}