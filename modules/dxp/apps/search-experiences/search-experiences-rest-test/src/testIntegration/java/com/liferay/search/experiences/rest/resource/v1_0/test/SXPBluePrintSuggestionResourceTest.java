package com.liferay.search.experiences.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.rest.client.dto.v1_0.SuggestionsContributorConfiguration;
import com.liferay.portal.search.rest.client.dto.v1_0.SuggestionsContributorResults;
import com.liferay.portal.search.rest.client.pagination.Page;
import com.liferay.portal.search.rest.resource.v1_0.test.SuggestionResourceTest;
import com.liferay.portal.test.rule.Inject;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(Arquillian.class)
public class SXPBluePrintSuggestionResourceTest extends SuggestionResourceTest {

	@Override
	@Test
	public void testPostSuggestionsPage() throws Exception {
		super.testPostSuggestionsPage();

		_testPostSuggestionsPageWithSXPBlueprintSuggestionsContributor();
		_testPostSuggestionsPageWithSXPBlueprintSuggestionsContributorWithGroupERCScope();
		_testPostSuggestionsPageWithSXPBlueprintSuggestionsContributorWithSearchExperiencesAttributes();
	}

	private void _testPostSuggestionsPageWithSXPBlueprintSuggestionsContributor()
		throws Exception {

		String suggestionsDisplayGroupGroupName = "Suggestions";

		SXPBlueprint sxpBlueprint = _sxpBlueprintLocalService.addSXPBlueprint(
			null, TestPropsValues.getUserId(), "{}",
			Collections.singletonMap(LocaleUtil.US, ""), null, "",
			Collections.singletonMap(
				LocaleUtil.US, RandomTestUtil.randomString()),
			_serviceContext);

		Page<SuggestionsContributorResults> page = _postSuggestionsPage(
			"http://localhost:8080/web/guest/home", "/search",
			testGroup.getGroupId(), "q", _layout.getPlid(), null,
			_journalArticle.getArticleId(),
			new SuggestionsContributorConfiguration[] {
				new SuggestionsContributorConfiguration() {
					{
						attributes = JSONUtil.put(
							"sxpBlueprintExternalReferenceCode",
							sxpBlueprint.getExternalReferenceCode());
						contributorName = "sxpBlueprint";
						displayGroupName = suggestionsDisplayGroupGroupName;
					}
				}
			});

		_assertSuggestionContributorResults(
			suggestionsDisplayGroupGroupName, page,
			_journalArticle.getTitle(_locale));
	}

	private void _testPostSuggestionsPageWithSXPBlueprintSuggestionsContributorWithGroupERCScope()
		throws Exception {

		SXPBlueprint sxpBlueprint = _sxpBlueprintLocalService.addSXPBlueprint(
			null, TestPropsValues.getUserId(), "{}",
			Collections.singletonMap(LocaleUtil.US, ""), null, "",
			Collections.singletonMap(
				LocaleUtil.US, RandomTestUtil.randomString()),
			_serviceContext);

		String suggestionsDisplayGroupGroupName = "Suggestions";

		Page<SuggestionsContributorResults> page = _postSuggestionsPage(
			"http://localhost:8080/web/guest/home", "/search", null, "q",
			_layout.getPlid(), testGroup.getExternalReferenceCode(),
			_journalArticle.getArticleId(),
			new SuggestionsContributorConfiguration[] {
				new SuggestionsContributorConfiguration() {
					{
						attributes = JSONUtil.put(
							"sxpBlueprintExternalReferenceCode",
							sxpBlueprint.getExternalReferenceCode());
						contributorName = "sxpBlueprint";
						displayGroupName = suggestionsDisplayGroupGroupName;
					}
				}
			});

		_assertSuggestionContributorResults(
			suggestionsDisplayGroupGroupName, page,
			_journalArticle.getTitle(_locale));
	}

	private void _testPostSuggestionsPageWithSXPBlueprintSuggestionsContributorWithSearchExperiencesAttributes()
		throws Exception {

		Class<?> clazz = getClass();

		SXPBlueprint sxpBlueprint = _sxpBlueprintLocalService.addSXPBlueprint(
			null, TestPropsValues.getUserId(),
			StringUtil.read(
				clazz,
				StringBundler.concat(
					"dependencies/", clazz.getSimpleName(),
					"._testPostSuggestionsPageWithSXPBlueprintSuggestions",
					"ContributorWithSearchExperiencesAttributes.json")),
			Collections.singletonMap(LocaleUtil.US, StringPool.BLANK), null,
			StringPool.BLANK,
			Collections.singletonMap(
				LocaleUtil.US, RandomTestUtil.randomString()),
			_serviceContext);

		String suggestionsDisplayGroupGroupName = "Suggestions";

		Page<SuggestionsContributorResults> page = _postSuggestionsPage(
			"http://localhost:8080/web/guest/home", "/search",
			testGroup.getGroupId(), "q", _layout.getPlid(), null,
			_journalArticle.getArticleId(),
			new SuggestionsContributorConfiguration[] {
				new SuggestionsContributorConfiguration() {
					{
						attributes = JSONUtil.put(
							"search.experiences.entry.class.pk",
							_journalArticle.getResourcePrimKey()
						).put(
							"sxpBlueprintExternalReferenceCode",
							sxpBlueprint.getExternalReferenceCode()
						);
						contributorName = "sxpBlueprint";
						displayGroupName = suggestionsDisplayGroupGroupName;
					}
				}
			});

		_assertSuggestionContributorResults(
			suggestionsDisplayGroupGroupName, page,
			_journalArticle.getTitle(_locale));
	}

	@Inject
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

}
