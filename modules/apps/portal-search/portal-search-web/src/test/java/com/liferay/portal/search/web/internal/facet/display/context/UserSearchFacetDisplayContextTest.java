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

package com.liferay.portal.search.web.internal.facet.display.context;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.internal.facet.display.context.builder.UserSearchFacetDisplayContextBuilder;
import com.liferay.portal.search.web.internal.user.facet.configuration.UserFacetPortletInstanceConfiguration;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.RenderRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Lino Alves
 */
public class UserSearchFacetDisplayContextTest {

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
	public void testEmptySearchResults() throws Exception {
		String paramValue = "";

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(paramValue, "count:desc");

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			userSearchFacetTermDisplayContexts.toString(), 0,
			userSearchFacetTermDisplayContexts.size());

		Assert.assertEquals(
			paramValue, userSearchFacetDisplayContext.getParamValue());
		Assert.assertTrue(userSearchFacetDisplayContext.isNothingSelected());
		Assert.assertTrue(userSearchFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testEmptySearchResultsWithPreviousSelection() throws Exception {
		String userName = RandomTestUtil.randomString();

		String paramValue = userName;

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(paramValue, "count:desc");

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			userSearchFacetTermDisplayContexts.toString(), 1,
			userSearchFacetTermDisplayContexts.size());

		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext =
			userSearchFacetTermDisplayContexts.get(0);

		Assert.assertEquals(
			0, userSearchFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			userName, userSearchFacetTermDisplayContext.getUserName());
		Assert.assertTrue(userSearchFacetTermDisplayContext.isSelected());
		Assert.assertTrue(
			userSearchFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			paramValue, userSearchFacetDisplayContext.getParamValue());
		Assert.assertFalse(userSearchFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(userSearchFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testOneTerm() throws Exception {
		String userName = RandomTestUtil.randomString();

		int count = RandomTestUtil.randomInt();

		setUpOneTermCollector(userName, count);

		String paramValue = "";

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(paramValue, "count:desc");

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			userSearchFacetTermDisplayContexts.toString(), 1,
			userSearchFacetTermDisplayContexts.size());

		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext =
			userSearchFacetTermDisplayContexts.get(0);

		Assert.assertEquals(
			count, userSearchFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			userName, userSearchFacetTermDisplayContext.getUserName());
		Assert.assertFalse(userSearchFacetTermDisplayContext.isSelected());
		Assert.assertTrue(
			userSearchFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			paramValue, userSearchFacetDisplayContext.getParamValue());
		Assert.assertTrue(userSearchFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(userSearchFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testOneTermWithPreviousSelection() throws Exception {
		String userName = RandomTestUtil.randomString();

		int count = RandomTestUtil.randomInt();

		setUpOneTermCollector(userName, count);

		String paramValue = userName;

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(paramValue, "count:desc");

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			userSearchFacetTermDisplayContexts.toString(), 1,
			userSearchFacetTermDisplayContexts.size());

		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext =
			userSearchFacetTermDisplayContexts.get(0);

		Assert.assertEquals(
			count, userSearchFacetTermDisplayContext.getFrequency());
		Assert.assertEquals(
			userName, userSearchFacetTermDisplayContext.getUserName());
		Assert.assertTrue(userSearchFacetTermDisplayContext.isSelected());
		Assert.assertTrue(
			userSearchFacetTermDisplayContext.isFrequencyVisible());

		Assert.assertEquals(
			paramValue, userSearchFacetDisplayContext.getParamValue());
		Assert.assertFalse(userSearchFacetDisplayContext.isNothingSelected());
		Assert.assertFalse(userSearchFacetDisplayContext.isRenderNothing());
	}

	@Test
	public void testOrderByTermFrequencyAscending() throws Exception {
		String userName = RandomTestUtil.randomString();

		List<TermCollector> termCollectors = new ArrayList<>();

		termCollectors.add(createTermCollector("zulu", 4));
		termCollectors.add(createTermCollector("alpha", 2));
		termCollectors.add(createTermCollector("delta", 1));
		termCollectors.add(createTermCollector("beta", 3));

		setUpMultipleTermCollectors(termCollectors);

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(userName, "count:asc");

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			"delta:1|alpha:2|beta:3|zulu:4",
			buildFrequencyString(userSearchFacetTermDisplayContexts));
	}

	@Test
	public void testOrderByTermFrequencyDescending() throws Exception {
		String userName = RandomTestUtil.randomString();

		List<TermCollector> termCollectors = new ArrayList<>();

		termCollectors.add(createTermCollector("zulu", 1));
		termCollectors.add(createTermCollector("alpha", 2));
		termCollectors.add(createTermCollector("delta", 4));
		termCollectors.add(createTermCollector("beta", 3));

		setUpMultipleTermCollectors(termCollectors);

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(userName, "count:desc");

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			"delta:4|beta:3|alpha:2|zulu:1",
			buildFrequencyString(userSearchFacetTermDisplayContexts));
	}

	@Test
	public void testOrderByTermValueAscending() throws Exception {
		String userName = RandomTestUtil.randomString();

		List<TermCollector> termCollectors = createTermCollectorsList(
			"zulu", "alpha", "delta", "beta");

		setUpMultipleTermCollectors(termCollectors);

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(userName, "key:asc");

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			"[alpha, beta, delta, zulu]",
			buildNameString(userSearchFacetTermDisplayContexts));
	}

	@Test
	public void testOrderByTermValueDescending() throws Exception {
		String userName = RandomTestUtil.randomString();

		List<TermCollector> termCollectors = createTermCollectorsList(
			"zulu", "alpha", "delta", "beta");

		setUpMultipleTermCollectors(termCollectors);

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			createDisplayContext(userName, "key:desc");

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts =
				userSearchFacetDisplayContext.getTermDisplayContexts();

		Assert.assertEquals(
			"[zulu, delta, beta, alpha]",
			buildNameString(userSearchFacetTermDisplayContexts));
	}

	protected String buildFrequencyString(
			List<UserSearchFacetTermDisplayContext>
				userSearchFacetTermDisplayContexts)
		throws Exception {

		StringBundler sb = new StringBundler(
			userSearchFacetTermDisplayContexts.size() * 4);

		for (UserSearchFacetTermDisplayContext
				userSearchFacetTermDisplayContext :
					userSearchFacetTermDisplayContexts) {

			sb.append(userSearchFacetTermDisplayContext.getUserName());
			sb.append(StringPool.COLON);
			sb.append(userSearchFacetTermDisplayContext.getFrequency());
			sb.append(StringPool.PIPE);
		}

		sb.setIndex(sb.index() - 1);

		return sb.toString();
	}

	protected String buildNameString(
			List<UserSearchFacetTermDisplayContext>
				userSearchFacetTermDisplayContexts)
		throws Exception {

		List<String> names = new ArrayList<>();

		for (UserSearchFacetTermDisplayContext
				userSearchFacetTermDisplayContext :
					userSearchFacetTermDisplayContexts) {

			names.add(userSearchFacetTermDisplayContext.getUserName());
		}

		return names.toString();
	}

	protected UserSearchFacetDisplayContext createDisplayContext(
			String paramValue, String order)
		throws Exception {

		UserSearchFacetDisplayContextBuilder
			userSearchFacetDisplayContextBuilder =
				new UserSearchFacetDisplayContextBuilder(getRenderRequest());

		userSearchFacetDisplayContextBuilder.setFacet(_facet);
		userSearchFacetDisplayContextBuilder.setParamValue(paramValue);
		userSearchFacetDisplayContextBuilder.setFrequenciesVisible(true);
		userSearchFacetDisplayContextBuilder.setFrequencyThreshold(0);
		userSearchFacetDisplayContextBuilder.setMaxTerms(0);
		userSearchFacetDisplayContextBuilder.setOrder(order);

		return userSearchFacetDisplayContextBuilder.build();
	}

	protected TermCollector createTermCollector(String userName, int count) {
		TermCollector termCollector = Mockito.mock(TermCollector.class);

		Mockito.doReturn(
			count
		).when(
			termCollector
		).getFrequency();

		Mockito.doReturn(
			userName
		).when(
			termCollector
		).getTerm();

		return termCollector;
	}

	protected List<TermCollector> createTermCollectorsList(
		String... userNames) {

		List<TermCollector> termCollectors = new ArrayList<>();

		for (String userName : userNames) {
			termCollectors.add(
				createTermCollector(userName, RandomTestUtil.randomInt()));
		}

		return termCollectors;
	}

	protected PortletDisplay getPortletDisplay() throws ConfigurationException {
		PortletDisplay portletDisplay = Mockito.mock(PortletDisplay.class);

		Mockito.doReturn(
			Mockito.mock(UserFacetPortletInstanceConfiguration.class)
		).when(
			portletDisplay
		).getPortletInstanceConfiguration(
			Mockito.any()
		);

		return portletDisplay;
	}

	protected RenderRequest getRenderRequest() throws ConfigurationException {
		RenderRequest renderRequest = Mockito.mock(RenderRequest.class);

		Mockito.doReturn(
			getThemeDisplay()
		).when(
			renderRequest
		).getAttribute(
			WebKeys.THEME_DISPLAY
		);

		return renderRequest;
	}

	protected ThemeDisplay getThemeDisplay() throws ConfigurationException {
		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.doReturn(
			getPortletDisplay()
		).when(
			themeDisplay
		).getPortletDisplay();

		return themeDisplay;
	}

	protected void setUpMultipleTermCollectors(
		List<TermCollector> termCollectors) {

		Mockito.doReturn(
			termCollectors
		).when(
			_facetCollector
		).getTermCollectors();
	}

	protected void setUpOneTermCollector(String userName, int count) {
		Mockito.doReturn(
			Collections.singletonList(createTermCollector(userName, count))
		).when(
			_facetCollector
		).getTermCollectors();
	}

	private final Facet _facet = Mockito.mock(Facet.class);
	private final FacetCollector _facetCollector = Mockito.mock(
		FacetCollector.class);

}