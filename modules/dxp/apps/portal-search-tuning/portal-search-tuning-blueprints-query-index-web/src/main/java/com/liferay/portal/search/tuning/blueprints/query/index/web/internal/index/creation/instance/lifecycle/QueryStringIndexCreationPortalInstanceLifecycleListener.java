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

package com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.creation.instance.lifecycle;

import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexName;
import com.liferay.portal.search.tuning.blueprints.query.index.index.name.QueryStringIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexCreator;
import com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryStringIndexReader;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = PortalInstanceLifecycleListener.class)
public class QueryStringIndexCreationPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		if (Objects.equals(
				_searchEngineInformation.getVendorString(), "Solr")) {

			return;
		}

		QueryStringIndexName queryStringIndexName =
			_queryStringIndexNameBuilder.getQueryStringIndexName(
				company.getCompanyId());

		if (!_queryStringIndexReader.isIndexExists(queryStringIndexName)) {
			_queryStringIndexCreator.create(queryStringIndexName);
		}
	}

	@Reference
	private QueryStringIndexCreator _queryStringIndexCreator;

	@Reference
	private QueryStringIndexNameBuilder _queryStringIndexNameBuilder;

	@Reference
	private QueryStringIndexReader _queryStringIndexReader;

	@Reference
	private SearchEngineInformation _searchEngineInformation;

}