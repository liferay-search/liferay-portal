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

package com.liferay.search.experiences.searchresponse.json.translator.internal.aggregations;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.internal.aggregation.metrics.AvgAggregationResultImpl;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Optional;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author André de Oliveira
 */
public class AggregationsJSONTranslationContributorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDefaultTranslation() {
		AggregationsJSONTranslationContributor
			aggregationsJSONTranslationContributor =
				createAggregationsJSONTranslationContributor();

		Optional<JSONObject> optional =
			aggregationsJSONTranslationContributor.translate(
				new AvgAggregationResultImpl("foo", 42), null);

		Assert.assertEquals(
			"{\"name\":\"foo\",\"value\":42}", String.valueOf(optional.get()));
	}

	protected AggregationsJSONTranslationContributor
		createAggregationsJSONTranslationContributor() {

		AggregationsJSONTranslationContributor
			aggregationsJSONTranslationContributor =
				new AggregationsJSONTranslationContributor();

		aggregationsJSONTranslationContributor.setJSONFactory(
			JSONFactoryUtil.getJSONFactory());

		aggregationsJSONTranslationContributor.
			setAggregationJSONTranslatorsHolder(
				Mockito.mock(AggregationJSONTranslatorsHolder.class));

		return aggregationsJSONTranslationContributor;
	}

}