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

package com.liferay.search.experiences.blueprints.test;

import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import com.liferay.expando.kernel.service.ExpandoTableLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portlet.expando.util.test.ExpandoTestUtil;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributes;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributesBuilder;
import com.liferay.search.experiences.blueprints.engine.attributes.BlueprintsAttributesBuilderFactory;
import com.liferay.search.experiences.blueprints.engine.cache.JSONDataProviderCache;
import com.liferay.search.experiences.blueprints.engine.exception.BlueprintsEngineException;
import com.liferay.search.experiences.blueprints.engine.util.BlueprintsEngineHelper;
import com.liferay.search.experiences.blueprints.facets.constants.FacetsBlueprintKeys;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.search.experiences.blueprints.service.BlueprintService;
import com.liferay.search.experiences.problems.ProblemsHolderBuilderFactory;
import com.liferay.wiki.model.WikiNode;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.service.WikiNodeLocalService;
import com.liferay.wiki.service.WikiPageLocalService;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Rule;

/**
 * @author Wade Cao
 */
public abstract class BaseBlueprintsTestCase {

	@Before
	public void setUp() throws Exception {
		group = GroupTestUtil.addGroup();

		serviceContext = ServiceContextTestUtil.getServiceContext(
			group, TestPropsValues.getUserId());

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);

		user = TestPropsValues.getUser();
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	protected BlogsEntry addBlogsEntry(String title, String content)
		throws Exception {

		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			user.getUserId(), title, content, serviceContext);

		_blogsEntries.add(blogsEntry);

		return blogsEntry;
	}

	protected Blueprint addCompanyBlueprint(
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String configuration, String selectedElements)
		throws Exception {

		Blueprint blueprint = blueprintService.addCompanyBlueprint(
			titleMap, descriptionMap, configuration, selectedElements,
			serviceContext);

		blueprints.add(blueprint);

		return blueprint;
	}

	protected void addExpandoColumn(int indexType, String... columns)
		throws Exception {

		ExpandoTable expandoTable = _expandoTableLocalService.fetchTable(
			group.getCompanyId(),
			_classNameLocalService.getClassNameId(JournalArticle.class),
			"CUSTOM_FIELDS");

		if (expandoTable == null) {
			expandoTable = _expandoTableLocalService.addTable(
				group.getCompanyId(),
				_classNameLocalService.getClassNameId(JournalArticle.class),
				"CUSTOM_FIELDS");

			_expandoTables.add(expandoTable);
		}

		for (String column : columns) {
			ExpandoColumn expandoColumn = ExpandoTestUtil.addColumn(
				expandoTable, column, indexType);

			_expandoColumns.add(expandoColumn);

			UnicodeProperties unicodeProperties =
				expandoColumn.getTypeSettingsProperties();

			unicodeProperties.setProperty(
				ExpandoColumnConstants.INDEX_TYPE, String.valueOf(indexType));

			expandoColumn.setTypeSettingsProperties(unicodeProperties);

			_expandoColumnLocalService.updateExpandoColumn(expandoColumn);
		}
	}

	protected Blueprint addGroupBlueprint(
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String configuration, String selectedElements)
		throws Exception {

		Blueprint blueprint = blueprintService.addGroupBlueprint(
			titleMap, descriptionMap, configuration, selectedElements,
			serviceContext);

		blueprints.add(blueprint);

		return blueprint;
	}

	protected JournalArticle addJournalArticle(
			long groupId, long folderId, String title, String content)
		throws Exception {

		return JournalTestUtil.addArticle(
			groupId, folderId, PortalUtil.getClassNameId(JournalArticle.class),
			HashMapBuilder.put(
				LocaleUtil.US, title
			).build(),
			null,
			HashMapBuilder.put(
				LocaleUtil.US, content
			).build(),
			LocaleUtil.getSiteDefault(), false, true, serviceContext);
	}

	protected JournalArticle addJournalArticle(long groupId, String title)
		throws Exception {

		return addJournalArticle(groupId, title, "");
	}

	protected JournalArticle addJournalArticle(
			long groupId, String title, String content)
		throws Exception {

		return JournalTestUtil.addArticle(
			groupId, 0, PortalUtil.getClassNameId(JournalArticle.class),
			HashMapBuilder.put(
				LocaleUtil.US, title
			).build(),
			null,
			HashMapBuilder.put(
				LocaleUtil.US, content
			).build(),
			LocaleUtil.getSiteDefault(), false, true, serviceContext);
	}

	protected JournalArticle addJournalArticle(String title, String content)
		throws Exception {

		return addJournalArticle(group.getGroupId(), title, content);
	}

	protected WikiNode addWikiNode() throws Exception {
		return _wikiNodeLocalService.addDefaultNode(
			user.getUserId(), serviceContext);
	}

	protected WikiPage addWikiPage(
			WikiNode wikiNode, String title, String content)
		throws Exception {

		WikiPage wikiPage = _wikiPageLocalService.addPage(
			user.getUserId(), wikiNode.getNodeId(), title, content, "Summary",
			false, serviceContext);

		_wikiPages.add(wikiPage);

		return wikiPage;
	}

	protected void assertSearch(
			Blueprint blueprint, String configurationString, String expected,
			String keywords, String selectedElementString)
		throws Exception {

		assertSearch(
			blueprint, configurationString, expected, "127.0.0.1", keywords,
			selectedElementString);
	}

	protected void assertSearch(
			Blueprint blueprint, String configurationString, String expected,
			String ipAddress, String keywords, String selectedElementString)
		throws Exception {

		SearchResponse searchResponse = _getSearchResponse(
			blueprint, configurationString, ipAddress, keywords,
			selectedElementString);

		DocumentsAssert.assertValues(
			searchResponse.getRequestString(),
			searchResponse.getDocumentsStream(), "localized_title_en_US",
			expected);
	}

	protected void assertSearchIgnoreRelevance(
			Blueprint blueprint, String configurationString, String expected,
			String keywords, String selectedElementString)
		throws Exception {

		SearchResponse searchResponse = _getSearchResponse(
			blueprint, configurationString, "127.0.0.1", keywords,
			selectedElementString);

		DocumentsAssert.assertValuesIgnoreRelevance(
			searchResponse.getRequestString(),
			searchResponse.getDocumentsStream(), "localized_title_en_US",
			expected);
	}

	protected JSONArray createJSONArray() {
		return JSONFactoryUtil.createJSONArray();
	}

	protected JSONObject getAdvancedConfiguration() {
		return JSONUtil.put(null, null);
	}

	protected JSONObject getAggregationConfiguration() {
		return JSONUtil.put(null, null);
	}

	protected BlueprintsAttributes getBlueprintsAttributes(
			String ipAddress, String keywords)
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
			user.getUserId()
		).addAttribute(
			"explain", true
		).addAttribute(
			"page", 1
		).addAttribute(
			"ip_address", ipAddress
		).addAttribute(
			"plid", TestPropsValues.getPlid()
		).addAttribute(
			"scope_group_id", group.getGroupId()
		).addAttribute(
			"timezone_id", getTimeZoneID()
		);

		return blueprintsAttributesBuilder.build();
	}

	protected JSONObject getConfigurationJSONObject(JSONArray queryJSONArray) {
		return JSONUtil.put(
			FacetsBlueprintKeys.CONFIGURATION_SECTION, getConfigurationSection()
		).put(
			"advanced_configuration", getAdvancedConfiguration()
		).put(
			"aggregation_configuration", getAggregationConfiguration()
		).put(
			"framework_configuration", getFrameworkConfiguration()
		).put(
			"highlight_configuration", getHightlightConfiguration()
		).put(
			"parameter_configuration", JSONUtil.put(null, null)
		).put(
			"query_configuration", queryJSONArray
		).put(
			"sort_configuration", getSortConfiguration()
		);
	}

	protected JSONObject getConfigurationSection() {
		return JSONUtil.put(null, null);
	}

	protected String getConfigurationString(JSONObject... jsonObjects) {
		JSONArray jsonArray = createJSONArray();

		if (jsonObjects != null) {
			for (JSONObject jsonObject : jsonObjects) {
				jsonArray.put(jsonObject);
			}
		}

		JSONObject configurationJSONObject = getConfigurationJSONObject(
			jsonArray);

		return configurationJSONObject.toString();
	}

	protected JSONObject getElementTemplateJSONObject(String resourceName)
		throws Exception {

		String boostWebContentsByKeywordMatchJsonString = StringUtil.read(
			getClass(), resourceName);

		return JSONFactoryUtil.createJSONObject(
			boostWebContentsByKeywordMatchJsonString);
	}

	protected JSONObject getFrameworkConfiguration() {
		return JSONUtil.put("apply_indexer_clauses", true);
	}

	protected String getGeolocationValue(double latitude, double longitude) {
		return StringBundler.concat(
			"{\"latitude\":", String.valueOf(latitude), ", \"longitude\":",
			String.valueOf(longitude), "}");
	}

	protected JSONObject getHightlightConfiguration() {
		return JSONUtil.put(null, null);
	}

	protected ConfigurationTemporarySwapper
			getIPStackConfigurationTemporarySwapper(
				String apiKey, String isEnabled, String ip)
		throws Exception {

		return new ConfigurationTemporarySwapper(
			"com.liferay.search.experiences.blueprints.ipstack.internal." +
				"configuration.IPStackConfiguration",
			_toDictionary(
				HashMapBuilder.put(
					"apiKey", apiKey
				).put(
					"isEnabled", isEnabled
				).put(
					"testIpAddress", ip
				).build()));
	}

	protected JSONObject getParameterConfiguration() {
		return JSONUtil.put(null, null);
	}

	protected String getSelectedElementString() throws Exception {
		return JSONUtil.put(
			"query_configuration", createJSONArray()
		).toString();
	}

	protected JSONArray getSortConfiguration() {
		return createJSONArray();
	}

	protected String getTimeZoneID() throws Exception {
		TimeZone timeZone = user.getTimeZone();

		return timeZone.getID();
	}

	protected void setupJsonDataProviderCache(
		String ipAddress, String city, double latitude, double longitude) {

		_jsonDataProviderCache.put(
			ipAddress,
			JSONUtil.put(
				"city", city
			).put(
				"latitude", latitude
			).put(
				"longitude", longitude
			));
	}

	protected JournalArticle updateJournalArticle(
			JournalArticle journalArticle, String title, String content)
		throws Exception {

		return JournalTestUtil.updateArticle(
			journalArticle, title, content, false, true, serviceContext);
	}

	@DeleteAfterTestRun
	protected List<Blueprint> blueprints = new ArrayList<>();

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
	protected User user;

	private SearchResponse _getSearchResponse(
			Blueprint blueprint, String configurationString, String ipAddress,
			String keywords, String selectedElementString)
		throws BlueprintsEngineException, Exception, PortalException {

		if (!Validator.isBlank(configurationString) &&
			!Validator.isBlank(selectedElementString)) {

			blueprint = blueprintService.updateBlueprint(
				blueprint.getBlueprintId(), blueprint.getTitleMap(),
				blueprint.getDescriptionMap(), configurationString,
				selectedElementString, serviceContext);
		}

		return blueprintsEngineHelper.search(
			blueprint, getBlueprintsAttributes(ipAddress, keywords),
			_problemsHolderBuilderFactory.builder());
	}

	private Dictionary<String, Object> _toDictionary(Map<String, String> map) {
		return new HashMapDictionary<>(new HashMap<String, Object>(map));
	}

	@DeleteAfterTestRun
	private final List<BlogsEntry> _blogsEntries = new ArrayList<>();

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@Inject
	private ExpandoColumnLocalService _expandoColumnLocalService;

	@DeleteAfterTestRun
	private final List<ExpandoColumn> _expandoColumns = new ArrayList<>();

	@Inject
	private ExpandoTableLocalService _expandoTableLocalService;

	@DeleteAfterTestRun
	private final List<ExpandoTable> _expandoTables = new ArrayList<>();

	@Inject
	private JSONDataProviderCache _jsonDataProviderCache;

	@Inject
	private ProblemsHolderBuilderFactory _problemsHolderBuilderFactory;

	@Inject
	private WikiNodeLocalService _wikiNodeLocalService;

	@Inject
	private WikiPageLocalService _wikiPageLocalService;

	@DeleteAfterTestRun
	private final List<WikiPage> _wikiPages = new ArrayList<>();

}