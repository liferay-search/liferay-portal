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
import com.liferay.search.experiences.rest.client.dto.v1_0.SXPBlueprint;
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
public class SXPBlueprintResourceTest extends BaseSXPBlueprintResourceTestCase {

	@After
	@Override
	public void tearDown() throws Exception {
		Page<SXPBlueprint> page = sxpBlueprintResource.getSXPBlueprintsPage(
			null, null, null, null);

		for (SXPBlueprint sxpBlueprint : page.getItems()) {
			String sxpBlueprintTitle = sxpBlueprint.getTitle();

			if (sxpBlueprintTitle.startsWith("_")) {
				sxpBlueprintResource.deleteSXPBlueprint(sxpBlueprint.getId());
			}
		}

		super.tearDown();
	}

	@Override
	@Test
	public void testGetSXPBlueprintExport() throws Exception {
		SXPBlueprint sxpBlueprint = randomSXPBlueprint();

		String title = sxpBlueprint.getTitle();
		String description = sxpBlueprint.getDescription();

		SXPBlueprint postSXPBlueprint = testPostSXPBlueprint_addSXPBlueprint(
			sxpBlueprint);

		HttpInvoker.HttpResponse httpResponse =
			sxpBlueprintResource.getSXPBlueprintExportHttpResponse(
				postSXPBlueprint.getId());

		String content = httpResponse.getContent();

		String schemaVersion = postSXPBlueprint.getSchemaVersion();

		String expectedContent =
			"{  \"schemaVersion\" : \"" + schemaVersion +
				"\",  \"title_i18n\" : {    \"en_US\" : \"" + title +
					"\"  },  \"configuration\" : { },  \"description_i18n\" :" +
						" {    \"en_US\" : \"" + description +
							"\"  },  \"elementInstances\" : [ ]}";

		Assert.assertEquals(expectedContent, content);
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetSXPBlueprint() throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetSXPBlueprintNotFound() throws Exception {
	}

	@Override
	@Test
	public void testPostSXPBlueprint() throws Exception {
		super.testPostSXPBlueprint();

		String description = "_" + RandomTestUtil.randomLong();
		String title = "_" + RandomTestUtil.randomLong();

		SXPBlueprint sxpBlueprint = SXPBlueprint.toDTO(
			JSONUtil.put(
				"description", description
			).put(
				"title", title
			).toJSONString());

		SXPBlueprint postSXPBlueprint = testPostSXPBlueprint_addSXPBlueprint(
			sxpBlueprint);

		sxpBlueprint.setCreateDate(postSXPBlueprint.getCreateDate());
		sxpBlueprint.setId(postSXPBlueprint.getId());
		sxpBlueprint.setModifiedDate(postSXPBlueprint.getModifiedDate());
		sxpBlueprint.setSchemaVersion(postSXPBlueprint.getSchemaVersion());
		sxpBlueprint.setUserName(postSXPBlueprint.getUserName());
		sxpBlueprint.setActions(postSXPBlueprint.getActions());

		Assert.assertEquals(
			sxpBlueprint.toString(), postSXPBlueprint.toString());

		assertValid(postSXPBlueprint);
	}

	@Override
	@Test
	public void testPostSXPBlueprintValidate() throws Exception {
		sxpBlueprintResource.postSXPBlueprintValidate("{}");
	}

	@Override
	protected SXPBlueprint randomSXPBlueprint() throws Exception {
		SXPBlueprint sxpBlueprint = super.randomSXPBlueprint();

		/* TODO Elasticsearch split tokens at letter-number transitions causes
		   failure in testGetSXPBlueprintsPageWithFilterStringEquals() */

		sxpBlueprint.setTitle("_" + RandomTestUtil.randomLong());

		sxpBlueprint.setTitle_i18n(
			Collections.singletonMap("en_US", sxpBlueprint.getTitle()));

		sxpBlueprint.setDescription_i18n(
			Collections.singletonMap("en_US", sxpBlueprint.getDescription()));

		return sxpBlueprint;
	}

	@Override
	protected SXPBlueprint testDeleteSXPBlueprint_addSXPBlueprint()
		throws Exception {

		return _addSXPBlueprint(randomSXPBlueprint());
	}

	@Override
	protected SXPBlueprint testGetSXPBlueprint_addSXPBlueprint()
		throws Exception {

		return _addSXPBlueprint(randomSXPBlueprint());
	}

	@Override
	protected SXPBlueprint testGetSXPBlueprintsPage_addSXPBlueprint(
			SXPBlueprint sxpBlueprint)
		throws Exception {

		return _addSXPBlueprint(sxpBlueprint);
	}

	@Override
	protected SXPBlueprint testGraphQLSXPBlueprint_addSXPBlueprint()
		throws Exception {

		return _addSXPBlueprint(randomSXPBlueprint());
	}

	@Override
	protected SXPBlueprint testPatchSXPBlueprint_addSXPBlueprint()
		throws Exception {

		return _addSXPBlueprint(randomSXPBlueprint());
	}

	@Override
	protected SXPBlueprint testPostSXPBlueprint_addSXPBlueprint(
			SXPBlueprint sxpBlueprint)
		throws Exception {

		return _addSXPBlueprint(sxpBlueprint);
	}

	@Override
	protected SXPBlueprint testPostSXPBlueprintCopy_addSXPBlueprint(
			SXPBlueprint sxpBlueprint)
		throws Exception {

		return _addSXPBlueprint(sxpBlueprint);
	}

	private SXPBlueprint _addSXPBlueprint(SXPBlueprint sxpBlueprint)
		throws Exception {

		return sxpBlueprintResource.postSXPBlueprint(sxpBlueprint);
	}

}