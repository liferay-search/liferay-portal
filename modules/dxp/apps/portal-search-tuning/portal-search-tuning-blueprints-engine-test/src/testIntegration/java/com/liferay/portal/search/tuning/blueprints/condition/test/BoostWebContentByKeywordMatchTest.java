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

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
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
import com.liferay.portal.search.tuning.blueprints.constants.json.values.EvaluationType;
import com.liferay.portal.search.tuning.blueprints.engine.constants.ReservedParameterNames;
import com.liferay.portal.search.tuning.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.portal.search.tuning.blueprints.facets.constants.FacetsBlueprintContributorKeys;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Collections;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Wade Cao
 */
@RunWith(Arquillian.class)
public class BoostWebContentByKeywordMatchTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group, TestPropsValues.getUserId());
	}

	@Test
	public void testAnyWordInCondition() throws Exception {
		JournalTestUtil.addArticle(
			_group.getGroupId(), 0,
			PortalUtil.getClassNameId(JournalArticle.class),
			HashMapBuilder.put(
				LocaleUtil.US, "Coca Cola"
			).build(),
			null,
			HashMapBuilder.put(
				LocaleUtil.US, "cola cola"
			).build(),
			LocaleUtil.getSiteDefault(), false, true, _serviceContext);

		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(), 0,
			PortalUtil.getClassNameId(JournalArticle.class),
			HashMapBuilder.put(
				LocaleUtil.US, "Pepsi Cola"
			).build(),
			null,
			HashMapBuilder.put(
				LocaleUtil.US, ""
			).build(),
			LocaleUtil.getSiteDefault(), false, true, _serviceContext);

		String articleId = journalArticle.getArticleId();

		Blueprint blueprint = _blueprintService.addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			_getConfigurationString(null), "", 1, _serviceContext);

		_assertSearch(blueprint, null, "[coca cola, pepsi cola]", "cola", null);

		String configurationString = _getConfigurationString(
			_getQueryFragmentJSONObject(
				articleId, 100, EvaluationType.ANY_WORD_IN.getjsonValue(),
				"cola"));

		String selectedFragmentString = _getSelectedFragmentString(
			articleId, 100, EvaluationType.ANY_WORD_IN.getjsonValue(), "cola");

		_assertSearch(
			blueprint, configurationString, "[pepsi cola, coca cola]", "cola",
			selectedFragmentString);

		configurationString = _getConfigurationString(
			_getQueryFragmentJSONObject(
				articleId, 100, EvaluationType.NOT_CONTAINS.getjsonValue(),
				"cola"));

		selectedFragmentString = _getSelectedFragmentString(
			articleId, 100, EvaluationType.NOT_CONTAINS.getjsonValue(), "cola");

		_assertSearch(
			blueprint, configurationString, "[coca cola, pepsi cola]", "cola",
			selectedFragmentString);
	}

	private void _assertSearch(
			Blueprint blueprint, String configurationString, String expected,
			String keywords, String selectedFragmentString)
		throws Exception {

		if (!Validator.isBlank(configurationString) &&
			!Validator.isBlank(selectedFragmentString)) {

			_blueprintService.updateBlueprint(
				blueprint.getBlueprintId(), blueprint.getTitleMap(),
				blueprint.getDescriptionMap(), configurationString,
				selectedFragmentString, _serviceContext);
		}

		SearchResponse searchResponse = _blueprintsEngineHelper.search(
			_getBlueprintsAttributes(keywords), new Messages(),
			blueprint.getBlueprintId());

		DocumentsAssert.assertValues(
			searchResponse.getRequestString(),
			searchResponse.getDocumentsStream(), "localized_title_en_US",
			expected);
	}

	private JSONArray _createJSONArray() {
		return JSONFactoryUtil.createJSONArray();
	}

	private BlueprintsAttributes _getBlueprintsAttributes(String keywords)
		throws Exception {

		BlueprintsAttributesBuilder blueprintsAttributesBuilder =
			_blueprintsAttributesBuilderFactory.builder();

		blueprintsAttributesBuilder.companyId(
			_group.getCompanyId()
		).keywords(
			keywords
		).locale(
			LocaleUtil.US
		).userId(
			_getUser().getUserId()
		).addAttribute(
			ReservedParameterNames.IP_ADDRESS.getKey(), "127.0.0.1"
		).addAttribute(
			ReservedParameterNames.PLID.getKey(), TestPropsValues.getPlid()
		).addAttribute(
			ReservedParameterNames.SCOPE_GROUP_ID.getKey(), _group.getGroupId()
		).addAttribute(
			ReservedParameterNames.TIMEZONE_ID.getKey(), _getTimeZoneID()
		);

		return blueprintsAttributesBuilder.build();
	}

	private String _getConfigurationString(JSONObject jsonObject) {
		JSONArray jsonArray = _createJSONArray();

		if (jsonObject != null) {
			jsonArray.put(jsonObject);
		}

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
			_createJSONArray()
		).put(
			FacetsBlueprintContributorKeys.CONFIGURATION_SECTION,
			_createJSONArray()
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
		).toString();
	}

	private JSONObject _getFragmentTemplateJSONObject() throws Exception {
		String boostWebContentsByKeywordMatchJsonString = StringUtil.read(
			getClass(),
			"/fragments/boost-web-contents-by-keyword-match-test.json");

		return JSONFactoryUtil.createJSONObject(
			boostWebContentsByKeywordMatchJsonString);
	}

	private JSONObject _getQueryFragmentJSONObject(
		String articleId, int boost, String evaluationType, String keywords) {

		return JSONUtil.put(
			"category", "conditional"
		).put(
			"clauses",
			_createJSONArray().put(
				JSONUtil.put(
					"context", "query"
				).put(
					"occur", "should"
				).put(
					"query",
					JSONUtil.put(
						"query",
						JSONUtil.put(
							"terms",
							JSONUtil.put(
								"articleId_String_sortable",
								_createJSONArray().put(articleId)
							).put(
								"boost", boost
							)))
				).put(
					"type", "wrapper"
				))
		).put(
			"conditions",
			_createJSONArray().put(
				JSONUtil.put(
					"configuration",
					JSONUtil.put(
						"evaluation_type", evaluationType
					).put(
						"parameter_name", "${keywords}"
					).put(
						"value", _createJSONArray().put(keywords)
					)))
		).put(
			"description",
			JSONUtil.put(
				"en_US", "Show selected Web Contents higher in the results")
		).put(
			"enabled", true
		).put(
			"icon", "thumbs-up"
		).put(
			"title",
			JSONUtil.put("en_US", "Boost Web Contents by Keyword Match")
		);
	}

	private String _getSelectedFragmentString(
			String articleId, int boost, String evaluationType, String keywords)
		throws Exception {

		return JSONUtil.put(
			"query_configuration",
			_createJSONArray().put(
				JSONUtil.put(
					"fragmentOutput",
					JSONUtil.put(
						"category", "conditional"
					).put(
						"clauses",
						_createJSONArray().put(
							JSONUtil.put(
								"context", "query"
							).put(
								"occur", "should"
							).put(
								"query",
								JSONUtil.put(
									"query",
									JSONUtil.put(
										"terms",
										JSONUtil.put(
											"articleId_String_sortable",
											_createJSONArray().put(articleId)
										).put(
											"boost", boost
										)))
							).put(
								"type", "wrapper"
							))
					).put(
						"conditions",
						_createJSONArray().put(
							JSONUtil.put(
								"configuration",
								JSONUtil.put(
									"evaluation_type", evaluationType
								).put(
									"parameter_name", "${keywords}"
								).put(
									"value", _createJSONArray().put(keywords)
								)))
					).put(
						"description",
						JSONUtil.put(
							"en_US",
							"Show selected Web Contents higher in the results")
					).put(
						"enabled", true
					).put(
						"icon", "thumbs-up"
					).put(
						"title",
						JSONUtil.put(
							"en_US", "Boost Web Contents by Keyword Match")
					)
				).put(
					"fragmentTemplateJSON",
					_getFragmentTemplateJSONObject().get("fragmentTemplateJSON")
				).put(
					"uiConfigurationJSON",
					_getFragmentTemplateJSONObject().get("uiConfigurationJSON")
				).put(
					"uiConfigurationValues",
					_getUIConfigurationValuesJSONObject(
						articleId, boost, keywords)
				))
		).toString();
	}

	private String _getTimeZoneID() throws Exception {
		User user = _getUser();

		TimeZone timeZone = user.getTimeZone();

		return timeZone.getID();
	}

	private JSONObject _getUIConfigurationValuesJSONObject(
		String articleId, int boost, String keywords) {

		return JSONUtil.put(
			"article_ids",
			_createJSONArray().put(
				JSONUtil.put(
					"label", articleId
				).put(
					"value", articleId
				))
		).put(
			"boost", boost
		).put(
			"values",
			_createJSONArray().put(
				JSONUtil.put(
					"label", keywords
				).put(
					"value", keywords
				))
		);
	}

	private User _getUser() throws Exception {
		return TestPropsValues.getUser();
	}

	@Inject
	private BlueprintsAttributesBuilderFactory
		_blueprintsAttributesBuilderFactory;

	@Inject
	private BlueprintsEngineHelper _blueprintsEngineHelper;

	@Inject
	private BlueprintService _blueprintService;

	@DeleteAfterTestRun
	private Group _group;

	private ServiceContext _serviceContext;

}