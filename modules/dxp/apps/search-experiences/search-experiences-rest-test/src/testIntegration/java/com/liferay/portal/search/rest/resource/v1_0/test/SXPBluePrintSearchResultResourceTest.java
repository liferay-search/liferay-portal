package com.liferay.portal.search.rest.resource.v1_0.test;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.SearchEngine;
import com.liferay.portal.kernel.search.highlight.HighlightUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.search.engine.ConnectionInformation;
import com.liferay.portal.search.engine.NodeInformation;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.rest.dto.v1_0.SearchRequestBody;
import com.liferay.portal.search.rest.dto.v1_0.SearchResult;
import com.liferay.portal.search.rest.pagination.SearchPage;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;

import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;
import org.junit.Assert;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author Rodrigo Guedes de Souza
 */
@FeatureFlags(featureFlags = @FeatureFlag(value = "LPS-179669"))
public class SXPBluePrintSearchResultResourceTest extends BaseSearchResultResourceTestCase {

	@Override
	@Test
	@TestInfo("LPD-57341")
	public void testPostSearchPage() throws Exception {
		super.testPostSearchPage();
		_testPostSearchPageWithHighlightConfiguration();
		_testPostSearchPageWithoutHighlightConfiguration();
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

	private String _getEndpoint(Map<String, String> parameters)
		throws Exception {

		StringBundler sb = new StringBundler((parameters.size() * 4) + 2);

		sb.append(_baseURI);
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

	protected SearchPage<SearchResult> _postSearchPage(
		Map<String, String> parameters, SearchRequestBody searchRequestBody)
		throws Exception {

		return _toSearchPage(
			HTTPTestUtil.invokeToJSONObject(
				searchRequestBody.toString(), _getEndpoint(parameters),
				Http.Method.POST));
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

	private boolean _isSearchEngineElasticsearch() {
		return StringUtil.startsWith(
			_searchEngineInformation.getVendorString(), "Elasticsearch");
	}

	protected String _getUserHighlightedFullName() {
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

	private String _baseURI = "search";

	protected Locale _locale;

	protected SearchEngine _searchEngine;

	@Inject
	private SearchEngineInformation _searchEngineInformation;

	protected ServiceContext _serviceContext;

	@Inject
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

	public User _user;

}
