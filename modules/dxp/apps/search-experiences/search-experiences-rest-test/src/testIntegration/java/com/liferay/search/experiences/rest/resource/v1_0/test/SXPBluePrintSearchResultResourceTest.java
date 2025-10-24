/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.petra.function.UnsafeTriConsumer;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.SearchEngine;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.search.highlight.HighlightUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.search.engine.ConnectionInformation;
import com.liferay.portal.search.engine.NodeInformation;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.rest.client.pagination.Page;
import com.liferay.portal.search.rest.dto.v1_0.SearchRequestBody;
import com.liferay.portal.search.rest.dto.v1_0.SearchResult;
import com.liferay.portal.search.rest.pagination.SearchPage;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;

import java.net.URLEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rodrigo Guedes de Souza
 */
@FeatureFlags(featureFlags = @FeatureFlag(value = "LPS-179669"))
@RunWith(Arquillian.class)
public class SXPBluePrintSearchResultResourceTest
	extends BaseSearchResultResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_locale = LocaleUtil.getSiteDefault();

		_searchEngine = _searchEngineHelper.getSearchEngine();

		_user = TestPropsValues.getUser();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testGroup, _user.getUserId());
	}

	@Override
	@Test
	public void testGetSearchPage() throws Exception {
		String scope = String.valueOf(testGroup.getGroupId());

		Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult> page =
			searchResultResource.getSearchPage(
				null, true, null, scope, null, null,
				com.liferay.portal.search.rest.client.pagination.Pagination.of(
					1, 10),
				null);

		long totalCount = page.getTotalCount();

		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult1 = testGetSearchPage_addSearchResult(
				randomSearchResult());

		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult2 = testGetSearchPage_addSearchResult(
				randomSearchResult());

		page = searchResultResource.getSearchPage(
			null, true, null, scope, null, null,
			com.liferay.portal.search.rest.client.pagination.Pagination.of(
				1, 10),
			null);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			searchResult1,
			(List<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>)
				page.getItems());
		assertContains(
			searchResult2,
			(List<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>)
				page.getItems());
		assertValid(page, testGetSearchPage_getExpectedActions());
	}

	@Override
	@Test
	public void testGetSearchPageWithFilterDateTimeEquals() throws Exception {
		List<EntityField> entityFields = getEntityFields(
			EntityField.Type.DATE_TIME);

		if (entityFields.isEmpty()) {
			return;
		}

		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult1 = randomSearchResult();

		searchResult1 = testGetSearchPage_addSearchResult(searchResult1);

		for (EntityField entityField : entityFields) {
			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				page = searchResultResource.getSearchPage(
					null, null, null, String.valueOf(testGroup.getGroupId()),
					searchResult1.getTitle(),
					getFilterString(entityField, "between", searchResult1),
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(1, 2),
					null);

			assertEquals(
				Collections.singletonList(searchResult1),
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)page.getItems());
		}
	}

	@Override
	@Test
	public void testGetSearchPageWithPagination() throws Exception {
		Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
			searchResultPage = searchResultResource.getSearchPage(
				null, true, null, null, null, null, null, null);

		int totalCount = GetterUtil.getInteger(
			searchResultPage.getTotalCount());

		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult1 = testGetSearchPage_addSearchResult(
				randomSearchResult());

		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult2 = testGetSearchPage_addSearchResult(
				randomSearchResult());

		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult3 = testGetSearchPage_addSearchResult(
				randomSearchResult());

		int pageSizeLimit = 500;

		if (totalCount >= (pageSizeLimit - 2)) {
			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				page1 = searchResultResource.getSearchPage(
					null, true, null, null, null, null,
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(
							(int)Math.ceil((totalCount + 1.0) / pageSizeLimit),
							pageSizeLimit),
					null);

			Assert.assertEquals(totalCount + 3, page1.getTotalCount());

			assertContains(
				searchResult1,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)page1.getItems());

			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				page2 = searchResultResource.getSearchPage(
					null, true, null, null, null, null,
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(
							(int)Math.ceil((totalCount + 2.0) / pageSizeLimit),
							pageSizeLimit),
					null);

			assertContains(
				searchResult2,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)page2.getItems());

			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				page3 = searchResultResource.getSearchPage(
					null, true, null, null, null, null,
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(
							(int)Math.ceil((totalCount + 3.0) / pageSizeLimit),
							pageSizeLimit),
					null);

			assertContains(
				searchResult3,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)page3.getItems());
		}
		else {
			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				page1 = searchResultResource.getSearchPage(
					null, true, null, null, null, null,
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(1, totalCount + 2),
					null);

			List<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				searchResults1 =
					(List
						<com.liferay.portal.search.rest.client.dto.v1_0.
							SearchResult>)page1.getItems();

			Assert.assertEquals(
				searchResults1.toString(), totalCount + 2,
				searchResults1.size());

			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				page2 = searchResultResource.getSearchPage(
					null, true, null, null, null, null,
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(2, totalCount + 2),
					null);

			Assert.assertEquals(totalCount + 3, page2.getTotalCount());

			List<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				searchResults2 =
					(List
						<com.liferay.portal.search.rest.client.dto.v1_0.
							SearchResult>)page2.getItems();

			Assert.assertEquals(
				searchResults2.toString(), 1, searchResults2.size());

			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				page3 = searchResultResource.getSearchPage(
					null, true, null, null, null, null,
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(1, (int)totalCount + 3),
					null);

			assertContains(
				searchResult1,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)page3.getItems());
			assertContains(
				searchResult2,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)page3.getItems());
			assertContains(
				searchResult3,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)page3.getItems());
		}
	}

	@Override
	@Test
	public void testGetSearchPageWithSortInteger() throws Exception {
	}

	@Override
	@Test
	@TestInfo("LPD-57341")
	public void testPostSearchPage() throws Exception {
		_testPostSearchPageWithHighlightConfiguration();
		_testPostSearchPageWithoutHighlightConfiguration();
	}

	@Override
	protected String[] getIgnoredEntityFieldNames() {
		return _IGNORED_ENTITY_FIELD_NAMES;
	}

	@Override
	protected com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			testGetSearchPage_addSearchResult(
				com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
					searchResult)
		throws Exception {

		JournalTestUtil.addArticle(
			testGroup.getGroupId(), searchResult.getTitle(),
			searchResult.getDescription());

		return searchResult;
	}

	@Override
	protected void testGetSearchPageWithFilter(
			String operator, EntityField.Type type)
		throws Exception {

		List<EntityField> entityFields = getEntityFields(type);

		if (entityFields.isEmpty()) {
			return;
		}

		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult1 = testGetSearchPage_addSearchResult(
				randomSearchResult());

		for (EntityField entityField : entityFields) {
			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				page = searchResultResource.getSearchPage(
					null, true, null, null, null,
					getFilterString(entityField, operator, searchResult1),
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(1, 2),
					null);

			assertEquals(
				Collections.singletonList(searchResult1),
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)page.getItems());
		}
	}

	@Override
	protected void testGetSearchPageWithSort(
			EntityField.Type type,
			UnsafeTriConsumer
				<EntityField,
				 com.liferay.portal.search.rest.client.dto.v1_0.SearchResult,
				 com.liferay.portal.search.rest.client.dto.v1_0.SearchResult,
				 Exception> unsafeTriConsumer)
		throws Exception {

		List<EntityField> entityFields = getEntityFields(type);

		if (entityFields.isEmpty()) {
			return;
		}

		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult1 = randomSearchResult();
		com.liferay.portal.search.rest.client.dto.v1_0.SearchResult
			searchResult2 = randomSearchResult();

		for (EntityField entityField : entityFields) {
			unsafeTriConsumer.accept(entityField, searchResult1, searchResult2);
		}

		searchResult1 = testGetSearchPage_addSearchResult(searchResult1);

		searchResult2 = testGetSearchPage_addSearchResult(searchResult2);

		Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult> page =
			searchResultResource.getSearchPage(
				null, true, null, null, null, null, null, null);

		for (EntityField entityField : entityFields) {
			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				ascPage = searchResultResource.getSearchPage(
					null, true, null, null, null, null,
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(1, (int)page.getTotalCount() + 1),
					entityField.getName() + ":asc");

			assertContains(
				searchResult1,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)ascPage.getItems());
			assertContains(
				searchResult2,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)ascPage.getItems());

			Page<com.liferay.portal.search.rest.client.dto.v1_0.SearchResult>
				descPage = searchResultResource.getSearchPage(
					null, true, null, null, null, null,
					com.liferay.portal.search.rest.client.pagination.Pagination.
						of(1, (int)page.getTotalCount() + 1),
					entityField.getName() + ":desc");

			assertContains(
				searchResult2,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)descPage.getItems());
			assertContains(
				searchResult1,
				(List
					<com.liferay.portal.search.rest.client.dto.v1_0.
						SearchResult>)descPage.getItems());
		}
	}

	private SXPBlueprint _addSXPBlueprint(boolean highlightingEnabled)
		throws Exception {

		JSONObject configurationJSONObject = JSONUtil.put(
			"advancedConfiguration",
			JSONUtil.put(
				"source",
				JSONUtil.put(
					"fetchSource", true
				).put(
					"includes",
					JSONFactoryUtil.createJSONArray(
					).put(
						"fullName"
					)
				))
		).put(
			"generalConfiguration",
			JSONUtil.put(
				"searchableAssetTypes",
				JSONUtil.put("com.liferay.portal.kernel.model.User"))
		).put(
			"queryConfiguration", JSONUtil.put("applyIndexerClauses", true)
		);

		if (highlightingEnabled) {
			configurationJSONObject.put(
				"highlightConfiguration",
				_createSXPBlueprintHighlightConfigurationJSON());
		}

		return _sxpBlueprintLocalService.addSXPBlueprint(
			null, _user.getUserId(), configurationJSONObject.toString(),
			Collections.singletonMap(_locale, StringPool.BLANK), null,
			StringPool.BLANK,
			Collections.singletonMap(_locale, RandomTestUtil.randomString()),
			_serviceContext);
	}

	private JSONObject _createSXPBlueprintHighlightConfigurationJSON() {
		return JSONUtil.put(
			"fields",
			JSONUtil.put(
				"fullName",
				JSONUtil.put(
					"fragment_size", 100
				).put(
					"number_of_fragments", 10
				))
		).put(
			"post_tags",
			JSONFactoryUtil.createJSONArray(
			).put(
				"</liferay-hl>"
			)
		).put(
			"pre_tags",
			JSONFactoryUtil.createJSONArray(
			).put(
				"<liferay-hl>"
			)
		).put(
			"require_field_match", true
		);
	}

	private String _getEndpoint(Map<String, String> parameters)
		throws Exception {

		StringBundler sb = new StringBundler((parameters.size() * 4) + 2);

		sb.append(_BASE_URI);
		sb.append("/v1.0/search?");

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			sb.append("&");
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(
				URLEncoder.encode(
					GetterUtil.getString(entry.getValue()), StringPool.UTF8));
		}

		return sb.toString();
	}

	private Version _getSearchEngineVersion() {
		List<ConnectionInformation> connectionInformationList =
			_searchEngineInformation.getConnectionInformationList();

		ConnectionInformation connectionInformation =
			connectionInformationList.get(0);

		List<NodeInformation> nodeInformationList =
			connectionInformation.getNodeInformationList();

		NodeInformation nodeInformation = nodeInformationList.get(0);

		return Version.parseVersion(nodeInformation.getVersion());
	}

	private Map<String, JSONArray> _getSearchFacets(JSONObject jsonObject) {
		JSONObject searchFacetsJSONObject = jsonObject.getJSONObject(
			"searchFacets");

		if (searchFacetsJSONObject == null) {
			return null;
		}

		Map<String, JSONArray> map = new HashMap<>();

		Iterator<String> iterator = searchFacetsJSONObject.keys();

		while (iterator.hasNext()) {
			String key = iterator.next();

			map.put(key, searchFacetsJSONObject.getJSONArray(key));
		}

		return map;
	}

	private String _getUserHighlightedFullName() {
		Version version = _getSearchEngineVersion();

		if (_isSearchEngineElasticsearch() &&
			(version.compareTo(Version.parseVersion("8.10.2")) >= 0)) {

			return StringBundler.concat(
				HighlightUtil.HIGHLIGHT_TAG_OPEN, _user.getFirstName(),
				StringPool.SPACE, _user.getLastName(),
				HighlightUtil.HIGHLIGHT_TAG_CLOSE);
		}

		return StringBundler.concat(
			HighlightUtil.HIGHLIGHT_TAG_OPEN, _user.getFirstName(),
			HighlightUtil.HIGHLIGHT_TAG_CLOSE, StringPool.SPACE,
			HighlightUtil.HIGHLIGHT_TAG_OPEN, _user.getLastName(),
			HighlightUtil.HIGHLIGHT_TAG_CLOSE);
	}

	private boolean _isSearchEngineElasticsearch() {
		return StringUtil.startsWith(
			_searchEngineInformation.getVendorString(), "Elasticsearch");
	}

	private SearchPage<SearchResult> _postSearchPage(
			Map<String, String> parameters, SearchRequestBody searchRequestBody)
		throws Exception {

		return _toSearchPage(
			HTTPTestUtil.invokeToJSONObject(
				searchRequestBody.toString(), _getEndpoint(parameters),
				Http.Method.POST));
	}

	private SearchPage<SearchResult>
			_postSearchPageWithSXPBlueprintConfiguration(
				String entryClassNames, String keywords,
				SXPBlueprint sxpBlueprint)
		throws Exception {

		SearchRequestBody searchRequestBody = new SearchRequestBody() {
			{
				attributes = HashMapBuilder.<String, Object>put(
					"search.experiences.blueprint.external.reference.code",
					sxpBlueprint.getExternalReferenceCode()
				).build();
			}
		};

		return _postSearchPage(
			HashMapBuilder.put(
				"entryClassNames", entryClassNames
			).put(
				"search", keywords
			).build(),
			searchRequestBody);
	}

	private void _testPostSearchPageWithHighlightConfiguration()
		throws Exception {

		if (Objects.equals(_searchEngine.getVendor(), "Solr")) {
			return;
		}

		SearchPage<SearchResult> searchPage =
			_postSearchPageWithSXPBlueprintConfiguration(
				_user.getModelClassName(), _user.getFullName(),
				_addSXPBlueprint(true));

		List<SearchResult> searchResults = ListUtil.fromCollection(
			searchPage.getItems());

		Assert.assertFalse(searchResults.isEmpty());

		int count = ListUtil.count(
			searchResults,
			searchResult -> Objects.equals(
				searchResult.getTitle(), _getUserHighlightedFullName()));

		Assert.assertTrue(count >= 1);
	}

	private void _testPostSearchPageWithoutHighlightConfiguration()
		throws Exception {

		if (Objects.equals(_searchEngine.getVendor(), "Solr")) {
			return;
		}

		SearchPage<SearchResult> searchPage =
			_postSearchPageWithSXPBlueprintConfiguration(
				_user.getModelClassName(), _user.getFullName(),
				_addSXPBlueprint(false));

		List<SearchResult> searchResults = ListUtil.fromCollection(
			searchPage.getItems());

		Assert.assertFalse(searchResults.isEmpty());

		int count = ListUtil.count(
			searchResults,
			searchResult -> Objects.equals(
				searchResult.getTitle(), _user.getFullName()));

		Assert.assertTrue(count >= 1);

		Assert.assertEquals(
			0,
			ListUtil.count(
				searchResults,
				searchResult -> Objects.equals(
					searchResult.getTitle(), _getUserHighlightedFullName())));
	}

	private SearchPage<SearchResult> _toSearchPage(JSONObject jsonObject)
		throws Exception {

		return SearchPage.of(
			null, null, _getSearchFacets(jsonObject),
			JSONUtil.toList(
				jsonObject.getJSONArray("items"),
				itemJSONObject -> SearchResult.toDTO(
					itemJSONObject.toString())),
			Pagination.of(
				jsonObject.getInt("page"), jsonObject.getInt("pageSize")),
			jsonObject.getLong("totalCount"));
	}

	private static final String _BASE_URI = "search";

	private static final String[] _IGNORED_ENTITY_FIELD_NAMES = {
		"cmsKind", "cmsRoot", "cmsSection", "dateDisplay", "dateExpiration",
		"datePublish", "dateReview", "folderId",
		"objectFolderExternalReferenceCode"
	};

	private Locale _locale;
	private SearchEngine _searchEngine;

	@Inject
	private SearchEngineHelper _searchEngineHelper;

	@Inject
	private SearchEngineInformation _searchEngineInformation;

	private ServiceContext _serviceContext;

	@Inject
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

	private User _user;

}