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

package com.liferay.search.experiences.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.search.experiences.rest.client.dto.v1_0.SXPElement;
import com.liferay.search.experiences.rest.client.http.HttpInvoker;
import com.liferay.search.experiences.rest.client.pagination.Page;

import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian Wing Shun Chan
 */
@RunWith(Arquillian.class)
public class SXPElementResourceTest extends BaseSXPElementResourceTestCase {

	@After
	@Override
	public void tearDown() throws Exception {
		Page<SXPElement> page1 = sxpElementResource.getSXPElementsPage(
			null, null, null, null);

		for (SXPElement sxpElement : page1.getItems()) {
			String sxpElementTitle = sxpElement.getTitle();

			if (sxpElementTitle.startsWith("_")) {
				sxpElementResource.deleteSXPElement(sxpElement.getId());
			}
		}

		super.tearDown();
	}

	@Override
	@Test
	public void testGetSXPElementExport() throws Exception {
		SXPElement sxpElement = randomSXPElement();

		String title = sxpElement.getTitle();
		String description = sxpElement.getDescription();

		SXPElement postSXPElement = testPostSXPElement_addSXPElement(
			sxpElement);

		HttpInvoker.HttpResponse httpResponse =
			sxpElementResource.getSXPElementExportHttpResponse(
				postSXPElement.getId());

		String content = httpResponse.getContent();

		String schemaVersion = postSXPElement.getSchemaVersion();

		Integer type = postSXPElement.getType();

		String expectedContent =
			"{  \"schemaVersion\" : \"" + schemaVersion +
				"\",  \"title_i18n\" : {    \"en_US\" : \"" + title +
					"\"  },  \"description_i18n\" : {    \"en_US\" : \"" +
						description + "\"  },  \"type\" : " + type +
							",  \"elementDefinition\" : { }}";

		Assert.assertEquals(expectedContent, content);
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetSXPElement() throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetSXPElementNotFound() throws Exception {
	}

	@Override
	@Test
	public void testPostSXPElement() throws Exception {
		super.testPostSXPElement();

		String description = "_" + RandomTestUtil.randomLong();
		String title = "_" + RandomTestUtil.randomLong();

		SXPElement sxpElement = SXPElement.toDTO(
			JSONUtil.put(
				"description", description
			).put(
				"title", title
			).toJSONString());

		SXPElement postSXPElement = testPostSXPElement_addSXPElement(
			sxpElement);

		sxpElement.setCreateDate(postSXPElement.getCreateDate());

		sxpElement.setDescription_i18n(
			Collections.singletonMap(
				LocaleUtil.toBCP47LanguageId(LocaleUtil.US), description));
		sxpElement.setId(postSXPElement.getId());
		sxpElement.setModifiedDate(postSXPElement.getModifiedDate());
		sxpElement.setReadOnly(false);
		sxpElement.setSchemaVersion(postSXPElement.getSchemaVersion());
		sxpElement.setTitle_i18n(
			Collections.singletonMap(
				LocaleUtil.toBCP47LanguageId(LocaleUtil.US), title));
		sxpElement.setType(0);
		sxpElement.setUserName(postSXPElement.getUserName());
		sxpElement.setActions(postSXPElement.getActions());

		Assert.assertEquals(sxpElement.toString(), postSXPElement.toString());

		assertValid(postSXPElement);
	}

	@Override
	@Test
	public void testPostSXPElementValidate() throws Exception {
		sxpElementResource.postSXPElementValidate("{}");
	}

	@Override
	protected SXPElement randomSXPElement() throws Exception {
		SXPElement sxpElement = super.randomSXPElement();

		/* TODO Elasticsearch split tokens at letter-number transitions causes
		   failure in testGetSXPElementsPageWithFilterStringEquals() */

		sxpElement.setTitle("_" + RandomTestUtil.randomLong());

		sxpElement.setTitle_i18n(
			Collections.singletonMap("en_US", sxpElement.getTitle()));

		sxpElement.setDescription_i18n(
			Collections.singletonMap("en_US", sxpElement.getDescription()));

		return sxpElement;
	}

	@Override
	protected SXPElement testDeleteSXPElement_addSXPElement() throws Exception {
		return _addSXPElement(randomSXPElement());
	}

	@Override
	protected SXPElement testGetSXPElement_addSXPElement() throws Exception {
		return _addSXPElement(randomSXPElement());
	}

	@Override
	protected SXPElement testGetSXPElementsPage_addSXPElement(
			SXPElement sxpElement)
		throws Exception {

		return _addSXPElement(sxpElement);
	}

	@Override
	protected SXPElement testGraphQLSXPElement_addSXPElement()
		throws Exception {

		return _addSXPElement(randomSXPElement());
	}

	@Override
	protected SXPElement testPatchSXPElement_addSXPElement() throws Exception {
		return _addSXPElement(randomSXPElement());
	}

	@Override
	protected SXPElement testPostSXPElement_addSXPElement(SXPElement sxpElement)
		throws Exception {

		return _addSXPElement(sxpElement);
	}

	@Override
	protected SXPElement testPostSXPElementCopy_addSXPElement(
			SXPElement sxpElement)
		throws Exception {

		return _addSXPElement(sxpElement);
	}

	private SXPElement _addSXPElement(SXPElement sxpElement) throws Exception {
		return sxpElementResource.postSXPElement(sxpElement);
	}

}