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

package com.liferay.search.experiences.internal.search.searcher.wip;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.rest.dto.v1_0.SearchResponse;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author André de Oliveira
 */
public class SearchResponseDTOTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	public static SearchResponse toDTO(String json) throws Exception {
		return ObjectMapperUtil.readValue(SearchResponse.class, json);
	}

	@Ignore
	@Test
	public void testDTO() throws Exception {
		String jsonString = _getJSON();

		Assert.assertEquals(jsonString, _remarshal(jsonString));
	}

	@Rule
	public TestName testName = new TestName();

	protected static void assertJSONEquals(
			String jsonString1, String jsonString2)
		throws Exception {

		Assert.assertEquals(
			JSONUtil.toString(JSONFactoryUtil.createJSONObject(jsonString1)),
			JSONUtil.toString(JSONFactoryUtil.createJSONObject(jsonString2)));
	}

	private String _getJSON() {
		Class<?> clazz = getClass();

		return ResourceUtil.getResourceAsString(
			clazz,
			StringBundler.concat(
				clazz.getSimpleName(), ".", testName.getMethodName(), ".json"));
	}

	private String _remarshal(String jsonString) throws Exception {
		return String.valueOf(toDTO(jsonString));
	}

}