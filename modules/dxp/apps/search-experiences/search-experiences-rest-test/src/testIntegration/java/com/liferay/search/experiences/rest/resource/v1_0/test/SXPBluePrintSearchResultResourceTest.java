package com.liferay.search.experiences.rest.resource.v1_0.test;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.search.rest.dto.v1_0.SearchRequestBody;
import com.liferay.portal.search.rest.dto.v1_0.SearchResult;
import com.liferay.portal.search.rest.pagination.SearchPage;
import com.liferay.portal.search.rest.resource.v1_0.test.SearchResultResourceTest;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@FeatureFlags(featureFlags = @FeatureFlag(value = "LPS-179669"))
public class SXPBluePrintSearchResultResourceTest extends
	SearchResultResourceTest {

	@Override
	@Test
	@TestInfo("LPD-57341")
	public void testPostSearchPage() throws Exception {
		super.testPostSearchPage();
		_testPostSearchPageWithHighlightConfiguration();
//		_testPostSearchPageWithoutHighlightConfiguration();
	}

	private void _testPostSearchPageWithHighlightConfiguration()
		throws Exception {

		if (Objects.equals(_searchEngine.getVendor(), "Solr")) {
			return;
		}

		SearchPage<SearchResult> searchPage =
			_postSearchPageWithSXPBlueprintConfiguration(
				super._user.getModelClassName(), _user.getFullName(),
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

//	private void _testPostSearchPageWithoutHighlightConfiguration()
//		throws Exception {
//
//		if (Objects.equals(_searchEngine.getVendor(), "Solr")) {
//			return;
//		}
//
//		SearchPage<SearchResult> searchPage =
//			_postSearchPageWithSXPBlueprintConfiguration(
//				_user.getModelClassName(), _user.getFullName(),
//				_addSXPBlueprint(false));
//
//		List<SearchResult> searchResults = ListUtil.fromCollection(
//			searchPage.getItems());
//
//		Assert.assertFalse(searchResults.isEmpty());
//
//		int count = ListUtil.count(
//			searchResults,
//			searchResult -> Objects.equals(
//				searchResult.getTitle(), _user.getFullName()));
//
//		Assert.assertTrue(count >= 1);
//
//		Assert.assertEquals(
//			0,
//			ListUtil.count(
//				searchResults,
//				searchResult -> Objects.equals(
//					searchResult.getTitle(), _getUserHighlightedFullName())));
//	}

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

	@Inject
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

}
