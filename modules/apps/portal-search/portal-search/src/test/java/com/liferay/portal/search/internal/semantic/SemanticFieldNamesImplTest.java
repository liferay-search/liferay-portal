/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.semantic;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.semantic.SemanticFieldNames;
import com.liferay.portal.search.semantic.SemanticTextEmbeddingProviderType;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rodrigo Guedes
 */
public class SemanticFieldNamesImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testElasticsearchProvidedAndLiferayProvidedNamesDoNotCollide() {
		String elasticsearchProvidedFieldName = _semanticFieldNames.fieldName(
			LocaleUtil.US,
			SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED,
			"journal_article", 1536);

		String liferayProvidedFieldName = _semanticFieldNames.fieldName(
			LocaleUtil.US, SemanticTextEmbeddingProviderType.LIFERAY_PROVIDED,
			"journal_article", 1536);

		Assert.assertNotEquals(
			elasticsearchProvidedFieldName, liferayProvidedFieldName);
	}

	@Test
	public void testElasticsearchProvidedFieldNameForUSLocale() {
		Assert.assertEquals(
			"journal_article_en_US_semantic",
			_semanticFieldNames.fieldName(
				LocaleUtil.US,
				SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED,
				"journal_article", 0));
	}

	@Test
	public void testElasticsearchProvidedFieldNameIsDeterministic() {
		String fieldName1 = _semanticFieldNames.fieldName(
			LocaleUtil.BRAZIL,
			SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED,
			"blog_entry", 0);

		String fieldName2 = _semanticFieldNames.fieldName(
			LocaleUtil.BRAZIL,
			SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED,
			"blog_entry", 0);

		Assert.assertEquals(fieldName1, fieldName2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testElasticsearchProvidedWithEmptyAssetTypeThrows() {
		_semanticFieldNames.fieldName(
			LocaleUtil.US,
			SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED, "", 0);
	}

	@Test
	public void testElasticsearchProvidedWithMultipleLocales() {
		Assert.assertEquals(
			"blog_entry_pt_BR_semantic",
			_semanticFieldNames.fieldName(
				LocaleUtil.BRAZIL,
				SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED,
				"blog_entry", 0));
		Assert.assertEquals(
			"blog_entry_ja_JP_semantic",
			_semanticFieldNames.fieldName(
				LocaleUtil.JAPAN,
				SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED,
				"blog_entry", 0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testElasticsearchProvidedWithNullAssetTypeThrows() {
		_semanticFieldNames.fieldName(
			LocaleUtil.US,
			SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED, null, 0);
	}

	@Test
	public void testLiferayProvidedFieldNameForUSLocale() {
		Assert.assertEquals(
			"text_embedding_1536_en_US",
			_semanticFieldNames.fieldName(
				LocaleUtil.US,
				SemanticTextEmbeddingProviderType.LIFERAY_PROVIDED, null,
				1536));
	}

	@Test
	public void testLiferayProvidedFieldNameIsDeterministic() {
		String fieldName1 = _semanticFieldNames.fieldName(
			LocaleUtil.US, SemanticTextEmbeddingProviderType.LIFERAY_PROVIDED,
			null, 768);

		String fieldName2 = _semanticFieldNames.fieldName(
			LocaleUtil.US, SemanticTextEmbeddingProviderType.LIFERAY_PROVIDED,
			null, 768);

		Assert.assertEquals(fieldName1, fieldName2);
	}

	@Test
	public void testLiferayProvidedWithMultipleDimensions() {
		Assert.assertEquals(
			"text_embedding_768_en_US",
			_semanticFieldNames.fieldName(
				LocaleUtil.US,
				SemanticTextEmbeddingProviderType.LIFERAY_PROVIDED, null, 768));
		Assert.assertEquals(
			"text_embedding_3072_pt_BR",
			_semanticFieldNames.fieldName(
				LocaleUtil.BRAZIL,
				SemanticTextEmbeddingProviderType.LIFERAY_PROVIDED, null,
				3072));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLiferayProvidedWithZeroDimensionsThrows() {
		_semanticFieldNames.fieldName(
			LocaleUtil.US, SemanticTextEmbeddingProviderType.LIFERAY_PROVIDED,
			null, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullLocaleThrows() {
		_semanticFieldNames.fieldName(
			null, SemanticTextEmbeddingProviderType.ELASTICSEARCH_PROVIDED,
			"journal_article", 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullSemanticTextEmbeddingProviderTypeThrows() {
		_semanticFieldNames.fieldName(
			LocaleUtil.US, null, "journal_article", 1536);
	}

	private final SemanticFieldNames _semanticFieldNames =
		new SemanticFieldNamesImpl();

}