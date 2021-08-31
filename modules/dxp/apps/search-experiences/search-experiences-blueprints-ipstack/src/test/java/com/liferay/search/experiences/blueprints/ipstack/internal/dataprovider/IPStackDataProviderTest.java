/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.blueprints.ipstack.internal.dataprovider;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.blueprints.engine.cache.JSONDataProviderCache;
import com.liferay.search.experiences.blueprints.ipstack.internal.configuration.IPStackConfiguration;
import com.liferay.search.experiences.problems.Problem;
import com.liferay.search.experiences.problems.ProblemBuilders;
import com.liferay.search.experiences.problems.ProblemsHolder;
import com.liferay.search.experiences.problems.ProblemsHolderBuilder;
import com.liferay.search.experiences.problems.internal.ProblemBuilderFactoryImpl;
import com.liferay.search.experiences.problems.internal.ProblemBuildersImpl;
import com.liferay.search.experiences.problems.internal.ProblemsHolderBuilderFactoryImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Petteri Karttunen
 */
public class IPStackDataProviderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		setUpIPStackDataProvider();
		setUpCache();
		setUpConfiguration();
	}

	@Test
	public void testBlankIPAddress() {
		ProblemsHolderBuilder problemsHolderBuilder =
			createMessagesHolderBuilder();

		assertIPAddress("", problemsHolderBuilder);
		assertMessages(1, problemsHolderBuilder.build());
	}

	@Test
	public void testNullIPAddress() {
		ProblemsHolderBuilder problemsHolderBuilder =
			createMessagesHolderBuilder();

		assertIPAddress(null, problemsHolderBuilder);
		assertMessages(1, problemsHolderBuilder.build());
	}

	@Test
	public void testPrivateIPAddress() {
		ProblemsHolderBuilder problemsHolderBuilder =
			createMessagesHolderBuilder();

		assertIPAddress("172.16.0.1", problemsHolderBuilder);
		assertMessages(1, problemsHolderBuilder.build());
	}

	protected void assertIPAddress(
		String ipAddress, ProblemsHolderBuilder problemsHolderBuilder) {

		Assert.assertEquals(
			Optional.empty(),
			_ipStackDataProvider.getGeoLocationData(
				ipAddress, problemsHolderBuilder));
	}

	protected void assertMessages(
		int messageCount, ProblemsHolder problemsHolder) {

		List<Problem> problems = problemsHolder.getAllProblems();

		Assert.assertEquals(problems.toString(), messageCount, problems.size());
	}

	protected ProblemBuilders createMessagesHelper() {
		ProblemBuildersImpl problemBuildersImpl = new ProblemBuildersImpl();

		problemBuildersImpl.setMessageBuilderFactory(
			new ProblemBuilderFactoryImpl());

		return problemBuildersImpl;
	}

	protected ProblemsHolderBuilder createMessagesHolderBuilder() {
		ProblemsHolderBuilderFactoryImpl problemsHolderBuilderFactoryImpl =
			new ProblemsHolderBuilderFactoryImpl();

		problemsHolderBuilderFactoryImpl.setMessageBuilderFactory(
			new ProblemBuilderFactoryImpl());

		return problemsHolderBuilderFactoryImpl.builder();
	}

	protected void setUpCache() {
		Mockito.when(
			_jsonDataProviderCache.getJSONObject(Matchers.anyString())
		).thenReturn(
			null
		);

		ReflectionTestUtil.setFieldValue(
			_ipStackDataProvider, "_jsonDataProviderCache",
			_jsonDataProviderCache);
	}

	protected void setUpConfiguration() {
		Mockito.when(
			_ipStackConfiguration.testIpAddress()
		).thenReturn(
			""
		);

		Mockito.when(
			_ipStackConfiguration.isEnabled()
		).thenReturn(
			true
		);

		Mockito.when(
			_ipStackConfiguration.cacheTimeout()
		).thenReturn(
			0
		);

		Mockito.when(
			_ipStackConfiguration.apiURL()
		).thenReturn(
			"http://www.example.com"
		);

		Mockito.when(
			_ipStackConfiguration.apiKey()
		).thenReturn(
			"23456"
		);

		ReflectionTestUtil.setFieldValue(
			_ipStackDataProvider, "_ipStackConfiguration",
			_ipStackConfiguration);
	}

	protected void setUpIPStackDataProvider() {
		_ipStackDataProvider = new IPStackDataProvider();

		_ipStackDataProvider.setMessagesHelper(createMessagesHelper());

		_ipStackDataProvider.activate(new HashMap<String, Object>());
	}

	@Mock
	private IPStackConfiguration _ipStackConfiguration;

	@Mock
	private IPStackDataProvider _ipStackDataProvider;

	@Mock
	private JSONDataProviderCache _jsonDataProviderCache;

}