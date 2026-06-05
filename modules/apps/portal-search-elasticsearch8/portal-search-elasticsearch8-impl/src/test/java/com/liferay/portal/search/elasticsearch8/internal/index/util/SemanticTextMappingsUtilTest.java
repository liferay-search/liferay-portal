/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.index.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.search.semantic.SemanticFieldNames;
import com.liferay.portal.search.semantic.SemanticTextEmbeddingProviderType;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Rodrigo Guedes de Souza
 */
public class SemanticTextMappingsUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_semanticFieldNames = Mockito.mock(SemanticFieldNames.class);

		Mockito.when(
			_semanticFieldNames.fieldName(
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
	}

	@Test
	public void testPutSemanticTextPropertiesEmptyAssetTypes() {
		JSONObject propertiesJSONObject = _jsonFactory.createJSONObject();

		SemanticTextMappingsUtil.putSemanticTextProperties(
			Collections.emptySet(), _INFERENCE_ID,
			SetUtil.fromArray(LocaleUtil.US), _semanticFieldNames,
			propertiesJSONObject);

		Assert.assertEquals(
			propertiesJSONObject.toString(), 0, propertiesJSONObject.length());
	}

	@Test
	public void testPutSemanticTextPropertiesEmptyLocales() {
		JSONObject propertiesJSONObject = _jsonFactory.createJSONObject();

		SemanticTextMappingsUtil.putSemanticTextProperties(
			SetUtil.fromArray("journal_article"), _INFERENCE_ID,
			Collections.emptySet(), _semanticFieldNames, propertiesJSONObject);

		Assert.assertEquals(
			propertiesJSONObject.toString(), 0, propertiesJSONObject.length());
	}

	@Test
	public void testPutSemanticTextPropertiesOneFieldPerAssetTypePerLocale() {
		Set<String> assetTypes = SetUtil.fromArray(
			"blog_entry", "journal_article");
		Set<Locale> locales = SetUtil.fromArray(
			LocaleUtil.BRAZIL, LocaleUtil.US);

		JSONObject propertiesJSONObject = _jsonFactory.createJSONObject();

		SemanticTextMappingsUtil.putSemanticTextProperties(
			assetTypes, _INFERENCE_ID, locales, _semanticFieldNames,
			propertiesJSONObject);

		Assert.assertEquals(
			propertiesJSONObject.toString(), assetTypes.size() * locales.size(),
			propertiesJSONObject.length());

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
	public void testPutSemanticTextPropertiesPreservesExistingProperties() {
		JSONObject propertiesJSONObject = JSONUtil.put(
			"title", JSONUtil.put("type", "text"));

		SemanticTextMappingsUtil.putSemanticTextProperties(
			SetUtil.fromArray("journal_article"), _INFERENCE_ID,
			SetUtil.fromArray(LocaleUtil.US), _semanticFieldNames,
			propertiesJSONObject);

		JSONObject titleJSONObject = propertiesJSONObject.getJSONObject(
			"title");

		Assert.assertEquals("text", titleJSONObject.getString("type"));

		Assert.assertNotNull(
			propertiesJSONObject.getJSONObject(
				"journal_article_en_US_semantic"));
	}

	private static final String _INFERENCE_ID = "liferay-active-provider";

	private final JSONFactory _jsonFactory = new JSONFactoryImpl();
	private SemanticFieldNames _semanticFieldNames;

}