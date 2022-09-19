/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.web.internal.custom.facet.display.context;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.internal.custom.facet.display.context.builder.CustomFacetDisplayContextBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Wade Cao
 */
public class CustomFacetDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		Mockito.doReturn(
			_facetCollector
		).when(
			_facet
		).getFacetCollector();
	}

	@Test
	public void testEmptyCustomDisplayCaption() throws Exception {
		String customDisplayCaption = "";
		String fieldToAggregate = "groupId";
		String parameterValue = "";

		CustomFacetDisplayContext customFacetDisplayContext =
			_createDisplayContext(
				customDisplayCaption, fieldToAggregate, parameterValue,
				"count:desc");

		List<CustomFacetTermDisplayContext> customFacetTermDisplayContexts =
			customFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			customFacetTermDisplayContexts.toString(), 0,
			customFacetTermDisplayContexts.size());

		Assert.assertTrue(customFacetDisplayContext.isNothingSelected());
		Assert.assertTrue(customFacetDisplayContext.isRenderNothing());
		Assert.assertEquals(
			fieldToAggregate, customFacetDisplayContext.getDisplayCaption());
	}

	@Test
	public void testEmptySearchResultsWithPreviousSelection() throws Exception {
		String fieldName = RandomTestUtil.randomString();

		String parameterValue = fieldName;

		CustomFacetDisplayContext customFacetDisplayContext =
			_createDisplayContext(
				"customDisplayCaption", "fieldToAggregate", parameterValue,
				"count:desc");

		List<CustomFacetTermDisplayContext> customFacetTermDisplayContexts =
			customFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			customFacetTermDisplayContexts.toString(), 1,
			customFacetTermDisplayContexts.size());

		CustomFacetTermDisplayContext customFacetTermDisplayContext =
			customFacetTermDisplayContexts.get(0);

		Assert.assertEquals(0, customFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			fieldName, customFacetTermDisplayContext.getFieldName());
		Assert.assertTrue(customFacetTermDisplayContext.isSelected());
		Assert.assertTrue(customFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			parameterValue, customFacetDisplayContext.getParameterValue());
		Assert.assertFalse(customFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(customFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testOneTerm() throws Exception {
		String fieldName = RandomTestUtil.randomString();

		_setUpTermCollectors(fieldName);

		String parameterValue = "";

		CustomFacetDisplayContext customFacetDisplayContext =
			_createDisplayContext(
				"customDisplayCaption", "fieldToAggregate", parameterValue,
				"count:desc");

		List<CustomFacetTermDisplayContext> customFacetTermDisplayContexts =
			customFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			customFacetTermDisplayContexts.toString(), 1,
			customFacetTermDisplayContexts.size());

		CustomFacetTermDisplayContext customFacetTermDisplayContext =
			customFacetTermDisplayContexts.get(0);

		Assert.assertEquals(1, customFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			fieldName, customFacetTermDisplayContext.getFieldName());
		Assert.assertFalse(customFacetTermDisplayContext.isSelected());
		Assert.assertTrue(customFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			parameterValue, customFacetDisplayContext.getParameterValue());
		Assert.assertTrue(customFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(customFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testOneTermWithPreviousSelection() throws Exception {
		String fieldName = RandomTestUtil.randomString();

		_setUpTermCollectors(fieldName);

		String parameterValue = fieldName;

		CustomFacetDisplayContext customFacetDisplayContext =
			_createDisplayContext(
				"customDisplayCaption", "fieldToAggregate", parameterValue,
				"count:desc");

		List<CustomFacetTermDisplayContext> customFacetTermDisplayContexts =
			customFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			customFacetTermDisplayContexts.toString(), 1,
			customFacetTermDisplayContexts.size());

		CustomFacetTermDisplayContext customFacetTermDisplayContext =
			customFacetTermDisplayContexts.get(0);

		Assert.assertEquals(1, customFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			fieldName, customFacetTermDisplayContext.getFieldName());
		Assert.assertTrue(customFacetTermDisplayContext.isSelected());
		Assert.assertTrue(customFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			parameterValue, customFacetDisplayContext.getParameterValue());
		Assert.assertFalse(customFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(customFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testOrderByTermFrequencyAscending() throws Exception {
		_setUpTermCollectors("alpha", "charlie", "bravo", "delta");

		String parameterValue = "";

		CustomFacetDisplayContext customFacetDisplayContext =
			_createDisplayContext(
				"customDisplayCaption", "fieldToAggregate", parameterValue,
				"count:asc");

		List<CustomFacetTermDisplayContext> customFacetTermDisplayContexts =
			customFacetDisplayContext.getTermDisplayContexts();

		String nameFrequencyString = _buildNameFrequencyString(
			customFacetTermDisplayContexts);

		Assert.assertEquals(
			customFacetTermDisplayContexts.toString(),
			"alpha:1|charlie:2|bravo:3|delta:4", nameFrequencyString);
	}

	@Test
	public void testOrderByTermFrequencyDescending() throws Exception {
		_setUpTermCollectors("alpha", "charlie", "bravo", "delta");

		String parameterValue = "";

		CustomFacetDisplayContext customFacetDisplayContext =
			_createDisplayContext(
				"customDisplayCaption", "fieldToAggregate", parameterValue,
				"count:desc");

		List<CustomFacetTermDisplayContext> customFacetTermDisplayContexts =
			customFacetDisplayContext.getTermDisplayContexts();

		String nameFrequencyString = _buildNameFrequencyString(
			customFacetTermDisplayContexts);

		Assert.assertEquals(
			customFacetTermDisplayContexts.toString(),
			"delta:4|bravo:3|charlie:2|alpha:1", nameFrequencyString);
	}

	private String _buildNameFrequencyString(
		List<CustomFacetTermDisplayContext> customFacetTermDisplayContexts) {

		StringBundler sb = new StringBundler(
			customFacetTermDisplayContexts.size() * 4);

		for (CustomFacetTermDisplayContext customFacetTermDisplayContext :
				customFacetTermDisplayContexts) {

			sb.append(customFacetTermDisplayContext.getFieldName());
			sb.append(StringPool.COLON);
			sb.append(customFacetTermDisplayContext.getFrequency());
			sb.append(StringPool.PIPE);
		}

		sb.setIndex(sb.index() - 1);

		return sb.toString();
	}

	private CustomFacetDisplayContext _createDisplayContext(
			String customDisplayCaption, String fieldToAggregate,
			String parameterValue, String order)
		throws Exception {

		CustomFacetDisplayContextBuilder customFacetDisplayContextBuilder =
			new CustomFacetDisplayContextBuilder(_getHttpServletRequest());

		customFacetDisplayContextBuilder.setCustomDisplayCaption(
			Optional.ofNullable(customDisplayCaption));
		customFacetDisplayContextBuilder.setFacet(_facet);
		customFacetDisplayContextBuilder.setFieldToAggregate(fieldToAggregate);
		customFacetDisplayContextBuilder.setFrequenciesVisible(true);
		customFacetDisplayContextBuilder.setFrequencyThreshold(0);
		customFacetDisplayContextBuilder.setMaxTerms(0);
		customFacetDisplayContextBuilder.setOrder(order);
		customFacetDisplayContextBuilder.setParameterName("custom");
		customFacetDisplayContextBuilder.setParameterValue(parameterValue);

		return customFacetDisplayContextBuilder.build();
	}

	private TermCollector _createTermCollector(String fieldName, int count) {
		TermCollector termCollector = Mockito.mock(TermCollector.class);

		Mockito.doReturn(
			count
		).when(
			termCollector
		).getFrequency();

		Mockito.doReturn(
			fieldName
		).when(
			termCollector
		).getTerm();

		return termCollector;
	}

	private HttpServletRequest _getHttpServletRequest() {
		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		Mockito.doReturn(
			_getThemeDisplay()
		).when(
			httpServletRequest
		).getAttribute(
			WebKeys.THEME_DISPLAY
		);

		return httpServletRequest;
	}

	private ThemeDisplay _getThemeDisplay() {
		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.doReturn(
			Mockito.mock(PortletDisplay.class)
		).when(
			themeDisplay
		).getPortletDisplay();

		return themeDisplay;
	}

	private void _setUpTermCollectors(String... fieldNames) {
		List<TermCollector> termCollectors = new ArrayList<>();

		for (int i = 0; i < fieldNames.length; i++) {
			termCollectors.add(_createTermCollector(fieldNames[i], i + 1));
		}

		Mockito.doReturn(
			termCollectors
		).when(
			_facetCollector
		).getTermCollectors();
	}

	private final Facet _facet = Mockito.mock(Facet.class);
	private final FacetCollector _facetCollector = Mockito.mock(
		FacetCollector.class);

}