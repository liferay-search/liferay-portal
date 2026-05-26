/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.index;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.search.capabilities.ExternalEmbeddingCapabilityGate;
import com.liferay.portal.search.capabilities.ExternalEmbeddingEligibility;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.semantic.SemanticFieldNames;
import com.liferay.portal.search.semantic.SemanticTextEmbeddingProviderType;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes
 */
public class MappingsHelperImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAddSemanticTextMappingsInferenceIdMatches()
		throws Exception {

		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_availableChecker());

		String mappings = _invokeAddSemanticTextMappings(
			mappingsHelperImpl, _baselineMappings());

		JSONObject jsonObject = _jsonFactory.createJSONObject(mappings);

		JSONObject propertiesJSONObject = jsonObject.getJSONObject(
			"properties");

		JSONObject fieldJSONObject = propertiesJSONObject.getJSONObject(
			"journal_article_en_US_semantic");

		Assert.assertEquals(
			_INFERENCE_ID, fieldJSONObject.getString("inference_id"));
	}

	@Test
	public void testAddSemanticTextMappingsOneFieldPerAssetTypePerLocale()
		throws Exception {

		Set<String> assetTypes = SetUtil.fromArray(
			"blog_entry", "journal_article");
		Set<Locale> locales = SetUtil.fromArray(
			LocaleUtil.BRAZIL, LocaleUtil.US);

		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_availableChecker(), assetTypes, locales);

		String mappings = _invokeAddSemanticTextMappings(
			mappingsHelperImpl, _baselineMappings());

		JSONObject jsonObject = _jsonFactory.createJSONObject(mappings);

		JSONObject propertiesJSONObject = jsonObject.getJSONObject(
			"properties");

		for (String assetType : assetTypes) {
			for (Locale locale : locales) {
				String fieldName = StringBundler.concat(
					assetType, StringPool.UNDERLINE,
					LocaleUtil.toLanguageId(locale), "_semantic");

				JSONObject fieldJSONObject = propertiesJSONObject.getJSONObject(
					fieldName);

				Assert.assertEquals(
					"semantic_text", fieldJSONObject.getString("type"));
				Assert.assertEquals(
					_INFERENCE_ID, fieldJSONObject.getString("inference_id"));
			}
		}
	}

	@Test
	public void testAddSemanticTextMappingsStaticPropertiesNotDynamicTemplates()
		throws Exception {

		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_availableChecker());

		String mappings = _invokeAddSemanticTextMappings(
			mappingsHelperImpl, _baselineMappings());

		JSONObject jsonObject = _jsonFactory.createJSONObject(mappings);

		String semanticFieldKey = "journal_article_en_US_semantic";

		JSONObject propertiesJSONObject = jsonObject.getJSONObject(
			"properties");

		Assert.assertTrue(propertiesJSONObject.has(semanticFieldKey));

		Assert.assertFalse(
			jsonObject.getJSONArray(
				"dynamic_templates"
			).toString(
			).contains(
				semanticFieldKey
			));
	}

	@Test
	public void testGetDefaultOrOverrideMappingsJSONObjectAvailable() {
		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_availableChecker());

		JSONObject jsonObject = _invokeGetDefaultOrOverrideMappingsJSONObject(
			mappingsHelperImpl);

		JSONObject propertiesJSONObject = jsonObject.getJSONObject(
			"properties");

		Assert.assertTrue(
			propertiesJSONObject.has("journal_article_en_US_semantic"));

		Assert.assertFalse(
			jsonObject.getJSONArray(
				"dynamic_templates"
			).toString(
			).contains(
				"template_text_embedding_"
			));
	}

	@Test
	public void testGetDefaultOrOverrideMappingsJSONObjectUnavailable() {
		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_unavailableChecker(_UNSUPPORTED_SEARCH_ENGINE_REASON));

		JSONObject jsonObject = _invokeGetDefaultOrOverrideMappingsJSONObject(
			mappingsHelperImpl);

		JSONObject propertiesJSONObject = jsonObject.getJSONObject(
			"properties");

		Assert.assertFalse(
			propertiesJSONObject.has("journal_article_en_US_semantic"));

		Assert.assertTrue(
			jsonObject.getJSONArray(
				"dynamic_templates"
			).toString(
			).contains(
				"template_text_embedding_768"
			));
	}

	@Test
	public void testIsElasticsearchProvidedCapabilityAvailableEmptyAssetTypes() {
		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_availableChecker(), Collections.emptySet(),
			SetUtil.fromArray(LocaleUtil.US));

		Assert.assertFalse(
			_invokeIsElasticsearchProvidedCapabilityAvailable(
				mappingsHelperImpl));
	}

	@Test
	public void testIsElasticsearchProvidedCapabilityAvailableEmptyLocales() {
		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_availableChecker(), SetUtil.fromArray("journal_article"),
			Collections.emptySet());

		Assert.assertFalse(
			_invokeIsElasticsearchProvidedCapabilityAvailable(
				mappingsHelperImpl));
	}

	@Test
	public void testIsElasticsearchProvidedCapabilityAvailableGateUnavailable() {
		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_unavailableChecker(_UNSUPPORTED_SEARCH_ENGINE_REASON));

		Assert.assertFalse(
			_invokeIsElasticsearchProvidedCapabilityAvailable(
				mappingsHelperImpl));
	}

	@Test
	public void testIsElasticsearchProvidedCapabilityAvailableHappyPath() {
		MappingsHelperImpl mappingsHelperImpl = _newMappingsHelperImpl(
			_availableChecker());

		Assert.assertTrue(
			_invokeIsElasticsearchProvidedCapabilityAvailable(
				mappingsHelperImpl));
	}

	@Test
	public void testIsElasticsearchProvidedCapabilityAvailableLegacyConstructor() {
		MappingsHelperImpl mappingsHelperImpl = new MappingsHelperImpl(
			null, null, null, null, null, null);

		Assert.assertFalse(
			_invokeIsElasticsearchProvidedCapabilityAvailable(
				mappingsHelperImpl));
	}

	private ExternalEmbeddingCapabilityGate _availableChecker() {
		ExternalEmbeddingCapabilityGate externalEmbeddingCapabilityGate =
			Mockito.mock(ExternalEmbeddingCapabilityGate.class);

		Mockito.when(
			externalEmbeddingCapabilityGate.check()
		).thenReturn(
			ExternalEmbeddingEligibility.available()
		);

		return externalEmbeddingCapabilityGate;
	}

	private String _baselineMappings() {
		JSONObject jsonObject = _jsonFactory.createJSONObject();

		return jsonObject.put(
			"dynamic_templates", _jsonFactory.createJSONArray()
		).put(
			"properties", _jsonFactory.createJSONObject()
		).toString();
	}

	private String _invokeAddSemanticTextMappings(
		MappingsHelperImpl mappingsHelperImpl, String mappings) {

		return ReflectionTestUtil.invoke(
			mappingsHelperImpl, "_addSemanticTextMappings",
			new Class<?>[] {String.class}, mappings);
	}

	private JSONObject _invokeGetDefaultOrOverrideMappingsJSONObject(
		MappingsHelperImpl mappingsHelperImpl) {

		return ReflectionTestUtil.invoke(
			mappingsHelperImpl, "_getDefaultOrOverrideMappingsJSONObject",
			new Class<?>[0]);
	}

	private boolean _invokeIsElasticsearchProvidedCapabilityAvailable(
		MappingsHelperImpl mappingsHelperImpl) {

		return ReflectionTestUtil.invoke(
			mappingsHelperImpl, "_isElasticsearchProvidedCapabilityAvailable",
			new Class<?>[0]);
	}

	private MappingsHelperImpl _newMappingsHelperImpl(
		ExternalEmbeddingCapabilityGate externalEmbeddingCapabilityGate) {

		return _newMappingsHelperImpl(
			externalEmbeddingCapabilityGate,
			SetUtil.fromArray("journal_article"),
			SetUtil.fromArray(LocaleUtil.US));
	}

	private MappingsHelperImpl _newMappingsHelperImpl(
		ExternalEmbeddingCapabilityGate externalEmbeddingCapabilityGate,
		Set<String> assetTypes, Set<Locale> locales) {

		return new MappingsHelperImpl(
			assetTypes, null, externalEmbeddingCapabilityGate, null,
			_INFERENCE_ID, _jsonFactory, null, locales, null,
			_searchEngineInformation(), _semanticFieldNames());
	}

	private SearchEngineInformation _searchEngineInformation() {
		SearchEngineInformation searchEngineInformation = Mockito.mock(
			SearchEngineInformation.class);

		Mockito.when(
			searchEngineInformation.getEmbeddingVectorDimensions()
		).thenReturn(
			new int[] {768}
		);

		return searchEngineInformation;
	}

	private SemanticFieldNames _semanticFieldNames() {
		SemanticFieldNames semanticFieldNames = Mockito.mock(
			SemanticFieldNames.class);

		Mockito.when(
			semanticFieldNames.fieldName(
				Mockito.any(Locale.class),
				Mockito.eq(
					SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED),
				Mockito.anyString(), Mockito.anyInt())
		).thenAnswer(
			invocationOnMock -> StringBundler.concat(
				invocationOnMock.getArgument(2, String.class),
				StringPool.UNDERLINE,
				LocaleUtil.toLanguageId(
					invocationOnMock.getArgument(0, Locale.class)),
				"_semantic")
		);

		return semanticFieldNames;
	}

	private ExternalEmbeddingCapabilityGate _unavailableChecker(String reason) {
		ExternalEmbeddingCapabilityGate externalEmbeddingCapabilityGate =
			Mockito.mock(ExternalEmbeddingCapabilityGate.class);

		Mockito.when(
			externalEmbeddingCapabilityGate.check()
		).thenReturn(
			ExternalEmbeddingEligibility.unavailable(reason)
		);

		return externalEmbeddingCapabilityGate;
	}

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private static final String _UNSUPPORTED_SEARCH_ENGINE_REASON =
		"semantic-search.external-embedding-capability." +
			"unsupported-search-engine";

	private final JSONFactory _jsonFactory = new JSONFactoryImpl();

}