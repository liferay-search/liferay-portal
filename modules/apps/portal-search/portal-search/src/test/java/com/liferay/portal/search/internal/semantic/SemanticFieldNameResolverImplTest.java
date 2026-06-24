/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.semantic;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.semantic.SemanticFieldNameResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rodrigo Guedes
 */
public class SemanticFieldNameResolverImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testElasticsearchProvidedAndLiferayProvidedNamesDoNotCollide() {
		String elasticsearchProvidedFieldName =
			_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
				LocaleUtil.US, "journal_article");

		String liferayProvidedFieldName =
			_semanticFieldNameResolver.resolveLiferayProvidedFieldName(
				LocaleUtil.US, 1536);

		Assert.assertNotEquals(
			elasticsearchProvidedFieldName, liferayProvidedFieldName);
	}

	@Test
	public void testElasticsearchProvidedFieldNameForUSLocale() {
		Assert.assertEquals(
			"journal_article_en_US_semantic",
			_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
				LocaleUtil.US, "journal_article"));
	}

	@Test
	public void testElasticsearchProvidedFieldNameIsDeterministic() {
		String fieldName1 =
			_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
				LocaleUtil.BRAZIL, "blog_entry");

		String fieldName2 =
			_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
				LocaleUtil.BRAZIL, "blog_entry");

		Assert.assertEquals(fieldName1, fieldName2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testElasticsearchProvidedWithEmptyAssetTypeThrows() {
		_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
			LocaleUtil.US, "");
	}

	@Test
	public void testElasticsearchProvidedWithMultipleLocales() {
		Assert.assertEquals(
			"blog_entry_pt_BR_semantic",
			_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
				LocaleUtil.BRAZIL, "blog_entry"));
		Assert.assertEquals(
			"blog_entry_ja_JP_semantic",
			_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
				LocaleUtil.JAPAN, "blog_entry"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testElasticsearchProvidedWithNullAssetTypeThrows() {
		_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
			LocaleUtil.US, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testElasticsearchProvidedWithNullLocaleThrows() {
		_semanticFieldNameResolver.resolveElasticsearchProvidedFieldName(
			null, "journal_article");
	}

	@Test
	public void testLiferayProvidedFieldNameForUSLocale() {
		Assert.assertEquals(
			"text_embedding_1536_en_US",
			_semanticFieldNameResolver.resolveLiferayProvidedFieldName(
				LocaleUtil.US, 1536));
	}

	@Test
	public void testLiferayProvidedFieldNameIsDeterministic() {
		String fieldName1 =
			_semanticFieldNameResolver.resolveLiferayProvidedFieldName(
				LocaleUtil.US, 768);

		String fieldName2 =
			_semanticFieldNameResolver.resolveLiferayProvidedFieldName(
				LocaleUtil.US, 768);

		Assert.assertEquals(fieldName1, fieldName2);
	}

	@Test
	public void testLiferayProvidedWithMultipleDimensions() {
		Assert.assertEquals(
			"text_embedding_768_en_US",
			_semanticFieldNameResolver.resolveLiferayProvidedFieldName(
				LocaleUtil.US, 768));
		Assert.assertEquals(
			"text_embedding_3072_pt_BR",
			_semanticFieldNameResolver.resolveLiferayProvidedFieldName(
				LocaleUtil.BRAZIL, 3072));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLiferayProvidedWithNullLocaleThrows() {
		_semanticFieldNameResolver.resolveLiferayProvidedFieldName(null, 768);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLiferayProvidedWithZeroDimensionsThrows() {
		_semanticFieldNameResolver.resolveLiferayProvidedFieldName(
			LocaleUtil.US, 0);
	}

	private final SemanticFieldNameResolver _semanticFieldNameResolver =
		new SemanticFieldNameResolverImpl();

}