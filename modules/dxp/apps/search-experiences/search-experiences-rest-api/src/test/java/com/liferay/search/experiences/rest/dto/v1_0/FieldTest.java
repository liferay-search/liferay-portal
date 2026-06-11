/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.rest.dto.v1_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class FieldTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testEscape() throws Exception {
		_testEscape(
			"es\\\\ca\\\"pe\\bES\\fCA\\nPE\\rRO\\tOM",
			"es\\ca\"pe\bES\fCA\nPE\rRO\tOM");
		_testEscape(
			"es\\\\\\\\ca\\\\\\\"pe\\\\\\bES\\\\\\fCA\\\\\\nPE" +
				"\\\\\\rRO\\\\\\tOM",
			"es\\\\ca\\\"pe\\\bES\\\fCA\\\nPE\\\rRO\\\tOM");
	}

	@Test
	public void testToStringDefaultValueCollection() throws Exception {
		_testToStringDefaultValue(
			Arrays.asList(
				_createEntry(_LABEL_BLOGS_ENTRY, _CLASS_NAME_BLOGS_ENTRY),
				_createEntry(
					_LABEL_JOURNAL_ARTICLE, _CLASS_NAME_JOURNAL_ARTICLE)));
	}

	@Test
	public void testToStringDefaultValueObjectArray() throws Exception {
		_testToStringDefaultValue(
			new Object[] {
				_createEntry(_LABEL_BLOGS_ENTRY, _CLASS_NAME_BLOGS_ENTRY),
				_createEntry(
					_LABEL_JOURNAL_ARTICLE, _CLASS_NAME_JOURNAL_ARTICLE)
			});
	}

	private Map<String, String> _createEntry(String label, String value) {
		return HashMapBuilder.put(
			_LABEL, label
		).put(
			_VALUE, value
		).build();
	}

	private void _testEscape(String escaped, String unescaped) {
		Field field1 = new Field();

		field1.setDefaultValue(unescaped);
		field1.setName(unescaped);

		Assert.assertEquals(
			StringBundler.concat(
				"{\"defaultValue\": \"", escaped, "\", \"name\": \"", escaped,
				"\"}"),
			field1.toString());

		Field field2 = Field.unsafeToDTO(field1.toString());

		Assert.assertEquals(unescaped, field2.getDefaultValue());
		Assert.assertEquals(unescaped, field2.getName());

		Assert.assertEquals(field1.toString(), field2.toString());
	}

	private void _testToStringDefaultValue(Object defaultValue) {
		Field field1 = new Field();

		field1.setDefaultValue(defaultValue);

		Field field2 = Field.unsafeToDTO(field1.toString());

		Object[] defaultValues = (Object[])field2.getDefaultValue();

		Assert.assertEquals(
			Arrays.toString(defaultValues), 2, defaultValues.length);

		Map<?, ?> firstEntryMap = (Map<?, ?>)defaultValues[0];

		Assert.assertEquals(_LABEL_BLOGS_ENTRY, firstEntryMap.get(_LABEL));
		Assert.assertEquals(_CLASS_NAME_BLOGS_ENTRY, firstEntryMap.get(_VALUE));

		Map<?, ?> secondEntryMap = (Map<?, ?>)defaultValues[1];

		Assert.assertEquals(_LABEL_JOURNAL_ARTICLE, secondEntryMap.get(_LABEL));
		Assert.assertEquals(
			_CLASS_NAME_JOURNAL_ARTICLE, secondEntryMap.get(_VALUE));
	}

	private static final String _CLASS_NAME_BLOGS_ENTRY =
		"com.liferay.blogs.model.BlogsEntry";

	private static final String _CLASS_NAME_JOURNAL_ARTICLE =
		"com.liferay.journal.model.JournalArticle";

	private static final String _LABEL = "label";

	private static final String _LABEL_BLOGS_ENTRY = "Blogs Entry";

	private static final String _LABEL_JOURNAL_ARTICLE = "Journal Article";

	private static final String _VALUE = "value";

}