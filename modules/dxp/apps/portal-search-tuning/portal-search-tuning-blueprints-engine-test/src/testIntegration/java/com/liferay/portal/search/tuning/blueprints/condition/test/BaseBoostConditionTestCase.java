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

package com.liferay.portal.search.tuning.blueprints.condition.test;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilder;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.BlueprintKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.AdvancedConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.QueryProcessingConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.advanced.SourceConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.framework.FrameworkConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.parameter.ParameterConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetsBlueprintContributorKeys;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.test.rule.Inject;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;

/**
 * @author Wade Cao
 */
public abstract class BaseBoostConditionTestCase {

	@Before
	public void setUp() throws Exception {
		group = GroupTestUtil.addGroup();

		serviceContext = ServiceContextTestUtil.getServiceContext(
			group, TestPropsValues.getUserId());
	}

	protected Blueprint addCompanyBlueprint(
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String configuration, String selectedElements, int type)
		throws Exception {

		return blueprintService.addCompanyBlueprint(
			titleMap, descriptionMap, configuration, selectedElements, type,
			serviceContext);
	}

	protected void assertSearch(
			Blueprint blueprint, String configurationString, String expected,
			String keywords, String selectedElementString)
		throws Exception {

		if (!Validator.isBlank(configurationString) &&
			!Validator.isBlank(selectedElementString)) {

			blueprint = blueprintService.updateBlueprint(
				blueprint.getBlueprintId(), blueprint.getTitleMap(),
				blueprint.getDescriptionMap(), configurationString,
				selectedElementString, serviceContext);
		}

		SearchResponse searchResponse = blueprintsEngineHelper.search(
			blueprint, getBlueprintsAttributes(keywords), new Messages());

		DocumentsAssert.assertValues(
			searchResponse.getRequestString(),
			searchResponse.getDocumentsStream(), "localized_title_en_US",
			expected);
	}

	protected JSONArray createJSONArray() {
		return JSONFactoryUtil.createJSONArray();
	}

	protected BlueprintsAttributes getBlueprintsAttributes(String keywords)
		throws Exception {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			blueprintsAttributesBuilderFactory.builder();

		blueprintsAttributesBuilder.companyId(
			group.getCompanyId()
		).keywords(
			keywords
		).locale(
			LocaleUtil.US
		).userId(
			getUser().getUserId()
		).addAttribute(
			ParameterConfigurationKeys.PAGE.getJsonKey(), 1
		).addAttribute(
			ReservedParameterNames.IP_ADDRESS.getKey(), "127.0.0.1"
		).addAttribute(
			ReservedParameterNames.PLID.getKey(), TestPropsValues.getPlid()
		).addAttribute(
			ReservedParameterNames.SCOPE_GROUP_ID.getKey(), group.getGroupId()
		).addAttribute(
			ReservedParameterNames.TIMEZONE_ID.getKey(), getTimeZoneID()
		);

		return blueprintsAttributesBuilder.build();
	}

	protected JSONObject getConfigurationJSONObject(JSONArray jsonArray) {
		return JSONUtil.put(
			BlueprintKeys.ADVANCED_CONFIGURATION.getJsonKey(),
			JSONUtil.put(
				AdvancedConfigurationKeys.QUERY_PROCESSING.getJsonKey(),
				JSONUtil.put(
					QueryProcessingConfigurationKeys.EXCLUDE_QUERY_CONTRIBUTORS.
						getJsonKey(),
					""
				).put(
					QueryProcessingConfigurationKeys.
						EXCLUDE_QUERY_POST_PROCESSORS.getJsonKey(),
					""
				)
			).put(
				"source",
				JSONUtil.put(
					SourceConfigurationKeys.FETCH_SOURCE.getJsonKey(), true
				).put(
					SourceConfigurationKeys.SOURCE_EXCLUDES.getJsonKey(), ""
				).put(
					SourceConfigurationKeys.SOURCE_INCLUDES.getJsonKey(), ""
				)
			)
		).put(
			BlueprintKeys.AGGREGATION_CONFIGURATION.getJsonKey(),
			createJSONArray()
		).put(
			FacetsBlueprintContributorKeys.CONFIGURATION_SECTION,
			createJSONArray()
		).put(
			BlueprintKeys.FRAMEWORK_CONFIGURATION.getJsonKey(),
			JSONUtil.put(
				FrameworkConfigurationKeys.APPLY_INDEXER_CLAUSES.getJsonKey(),
				true)
		).put(
			BlueprintKeys.PARAMETER_CONFIGURATION.getJsonKey(),
			JSONUtil.put(null, null)
		).put(
			BlueprintKeys.QUERY_CONFIGURATION.getJsonKey(), jsonArray
		).put(
			BlueprintKeys.SORT_CONFIGURATION.getJsonKey(),
			JSONUtil.put(null, null)
		);
	}

	protected JSONObject getElementTemplateJSONObject(String resourceName)
		throws Exception {

		String boostWebContentsByKeywordMatchJsonString = StringUtil.read(
			getClass(), resourceName);

		return JSONFactoryUtil.createJSONObject(
			boostWebContentsByKeywordMatchJsonString);
	}

	protected String getTimeZoneID() throws Exception {
		User user = getUser();

		TimeZone timeZone = user.getTimeZone();

		return timeZone.getID();
	}

	protected User getUser() throws Exception {
		return TestPropsValues.getUser();
	}

	@Inject
	protected BlueprintsAttributesBuilderFactory
		blueprintsAttributesBuilderFactory;

	@Inject
	protected BlueprintsEngineHelper blueprintsEngineHelper;

	@Inject
	protected BlueprintService blueprintService;

	@DeleteAfterTestRun
	protected Group group;

	protected ServiceContext serviceContext;

}