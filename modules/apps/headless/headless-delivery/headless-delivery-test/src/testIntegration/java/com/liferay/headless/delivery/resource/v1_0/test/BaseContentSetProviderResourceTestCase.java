/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalServiceUtil;
import com.liferay.headless.delivery.client.dto.v1_0.ContentSetProvider;
import com.liferay.headless.delivery.client.dto.v1_0.Field;
import com.liferay.headless.delivery.client.http.HttpInvoker;
import com.liferay.headless.delivery.client.pagination.Page;
import com.liferay.headless.delivery.client.pagination.Pagination;
import com.liferay.headless.delivery.client.resource.v1_0.ContentSetProviderResource;
import com.liferay.headless.delivery.client.serdes.v1_0.ContentSetProviderSerDes;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import jakarta.annotation.Generated;

import jakarta.ws.rs.core.MultivaluedHashMap;

import java.lang.reflect.Method;

import java.text.Format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public abstract class BaseContentSetProviderResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_format = FastDateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		irrelevantDepotEntry = DepotEntryLocalServiceUtil.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, DepotConstants.TYPE_ASSET_LIBRARY,
			new ServiceContext() {
				{
					setCompanyId(testCompany.getCompanyId());
					setUserId(TestPropsValues.getUserId());
				}
			});
		irrelevantDepotEntryGroup = irrelevantDepotEntry.getGroup();
		testDepotEntry = DepotEntryLocalServiceUtil.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, DepotConstants.TYPE_ASSET_LIBRARY,
			new ServiceContext() {
				{
					setCompanyId(testCompany.getCompanyId());
					setUserId(TestPropsValues.getUserId());
				}
			});
		testDepotEntryGroup = testDepotEntry.getGroup();

		_contentSetProviderResource.setContextCompany(testCompany);

		_testCompanyAdminUser = UserTestUtil.getAdminUser(
			testCompany.getCompanyId());

		contentSetProviderResource = ContentSetProviderResource.builder(
		).authentication(
			_testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		DepotEntryLocalServiceUtil.deleteDepotEntry(irrelevantDepotEntry);
		DepotEntryLocalServiceUtil.deleteDepotEntry(testDepotEntry);

		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testClientSerDesToDTO() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		ContentSetProvider contentSetProvider1 = randomContentSetProvider();

		String json = objectMapper.writeValueAsString(contentSetProvider1);

		ContentSetProvider contentSetProvider2 = ContentSetProviderSerDes.toDTO(
			json);

		Assert.assertTrue(equals(contentSetProvider1, contentSetProvider2));
	}

	@Test
	public void testClientSerDesToJSON() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		ContentSetProvider contentSetProvider = randomContentSetProvider();

		String json1 = objectMapper.writeValueAsString(contentSetProvider);
		String json2 = ContentSetProviderSerDes.toJSON(contentSetProvider);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	protected ObjectMapper getClientSerDesObjectMapper() {
		return new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				enable(SerializationFeature.INDENT_OUTPUT);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		ContentSetProvider contentSetProvider = randomContentSetProvider();

		contentSetProvider.setItemSubtype(regex);
		contentSetProvider.setItemType(regex);
		contentSetProvider.setKey(regex);
		contentSetProvider.setTitle(regex);

		String json = ContentSetProviderSerDes.toJSON(contentSetProvider);

		Assert.assertFalse(json.contains(regex));

		contentSetProvider = ContentSetProviderSerDes.toDTO(json);

		Assert.assertEquals(regex, contentSetProvider.getItemSubtype());
		Assert.assertEquals(regex, contentSetProvider.getItemType());
		Assert.assertEquals(regex, contentSetProvider.getKey());
		Assert.assertEquals(regex, contentSetProvider.getTitle());
	}

	@Test
	public void testGetAssetLibraryContentSetProvidersPage() throws Exception {
		Long assetLibraryId =
			testGetAssetLibraryContentSetProvidersPage_getAssetLibraryId();
		Long irrelevantAssetLibraryId =
			testGetAssetLibraryContentSetProvidersPage_getIrrelevantAssetLibraryId();

		Page<ContentSetProvider> page =
			contentSetProviderResource.getAssetLibraryContentSetProvidersPage(
				assetLibraryId, RandomTestUtil.randomString(), null,
				Pagination.of(1, 10));

		long totalCount = page.getTotalCount();

		if (irrelevantAssetLibraryId != null) {
			ContentSetProvider irrelevantContentSetProvider =
				testGetAssetLibraryContentSetProvidersPage_addContentSetProvider(
					irrelevantAssetLibraryId,
					randomIrrelevantContentSetProvider());

			page =
				contentSetProviderResource.
					getAssetLibraryContentSetProvidersPage(
						irrelevantAssetLibraryId, null, null,
						Pagination.of(1, (int)totalCount + 1));

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantContentSetProvider,
				(List<ContentSetProvider>)page.getItems());
			assertValid(
				page,
				testGetAssetLibraryContentSetProvidersPage_getExpectedActions(
					irrelevantAssetLibraryId));
		}

		ContentSetProvider contentSetProvider1 =
			testGetAssetLibraryContentSetProvidersPage_addContentSetProvider(
				assetLibraryId, randomContentSetProvider());

		ContentSetProvider contentSetProvider2 =
			testGetAssetLibraryContentSetProvidersPage_addContentSetProvider(
				assetLibraryId, randomContentSetProvider());

		page =
			contentSetProviderResource.getAssetLibraryContentSetProvidersPage(
				assetLibraryId, null, null, Pagination.of(1, 10));

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			contentSetProvider1, (List<ContentSetProvider>)page.getItems());
		assertContains(
			contentSetProvider2, (List<ContentSetProvider>)page.getItems());
		assertValid(
			page,
			testGetAssetLibraryContentSetProvidersPage_getExpectedActions(
				assetLibraryId));
	}

	protected Map<String, Map<String, String>>
			testGetAssetLibraryContentSetProvidersPage_getExpectedActions(
				Long assetLibraryId)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	@Test
	public void testGetAssetLibraryContentSetProvidersPageWithPagination()
		throws Exception {

		Long assetLibraryId =
			testGetAssetLibraryContentSetProvidersPage_getAssetLibraryId();

		Page<ContentSetProvider> contentSetProvidersPage =
			contentSetProviderResource.getAssetLibraryContentSetProvidersPage(
				assetLibraryId, null, null, null);

		int totalCount = GetterUtil.getInteger(
			contentSetProvidersPage.getTotalCount());

		ContentSetProvider contentSetProvider1 =
			testGetAssetLibraryContentSetProvidersPage_addContentSetProvider(
				assetLibraryId, randomContentSetProvider());

		ContentSetProvider contentSetProvider2 =
			testGetAssetLibraryContentSetProvidersPage_addContentSetProvider(
				assetLibraryId, randomContentSetProvider());

		ContentSetProvider contentSetProvider3 =
			testGetAssetLibraryContentSetProvidersPage_addContentSetProvider(
				assetLibraryId, randomContentSetProvider());

		// See com.liferay.portal.vulcan.internal.configuration.HeadlessAPICompanyConfiguration#pageSizeLimit

		int pageSizeLimit = 500;

		if (totalCount >= (pageSizeLimit - 2)) {
			Page<ContentSetProvider> page1 =
				contentSetProviderResource.
					getAssetLibraryContentSetProvidersPage(
						assetLibraryId, null, null,
						Pagination.of(
							(int)Math.ceil((totalCount + 1.0) / pageSizeLimit),
							pageSizeLimit));

			Assert.assertEquals(totalCount + 3, page1.getTotalCount());

			assertContains(
				contentSetProvider1,
				(List<ContentSetProvider>)page1.getItems());

			Page<ContentSetProvider> page2 =
				contentSetProviderResource.
					getAssetLibraryContentSetProvidersPage(
						assetLibraryId, null, null,
						Pagination.of(
							(int)Math.ceil((totalCount + 2.0) / pageSizeLimit),
							pageSizeLimit));

			assertContains(
				contentSetProvider2,
				(List<ContentSetProvider>)page2.getItems());

			Page<ContentSetProvider> page3 =
				contentSetProviderResource.
					getAssetLibraryContentSetProvidersPage(
						assetLibraryId, null, null,
						Pagination.of(
							(int)Math.ceil((totalCount + 3.0) / pageSizeLimit),
							pageSizeLimit));

			assertContains(
				contentSetProvider3,
				(List<ContentSetProvider>)page3.getItems());
		}
		else {
			Page<ContentSetProvider> page1 =
				contentSetProviderResource.
					getAssetLibraryContentSetProvidersPage(
						assetLibraryId, null, null,
						Pagination.of(1, totalCount + 2));

			List<ContentSetProvider> contentSetProviders1 =
				(List<ContentSetProvider>)page1.getItems();

			Assert.assertEquals(
				contentSetProviders1.toString(), totalCount + 2,
				contentSetProviders1.size());

			Page<ContentSetProvider> page2 =
				contentSetProviderResource.
					getAssetLibraryContentSetProvidersPage(
						assetLibraryId, null, null,
						Pagination.of(2, totalCount + 2));

			Assert.assertEquals(totalCount + 3, page2.getTotalCount());

			List<ContentSetProvider> contentSetProviders2 =
				(List<ContentSetProvider>)page2.getItems();

			Assert.assertEquals(
				contentSetProviders2.toString(), 1,
				contentSetProviders2.size());

			Page<ContentSetProvider> page3 =
				contentSetProviderResource.
					getAssetLibraryContentSetProvidersPage(
						assetLibraryId, null, null,
						Pagination.of(1, (int)totalCount + 3));

			assertContains(
				contentSetProvider1,
				(List<ContentSetProvider>)page3.getItems());
			assertContains(
				contentSetProvider2,
				(List<ContentSetProvider>)page3.getItems());
			assertContains(
				contentSetProvider3,
				(List<ContentSetProvider>)page3.getItems());
		}
	}

	protected ContentSetProvider
			testGetAssetLibraryContentSetProvidersPage_addContentSetProvider(
				Long assetLibraryId, ContentSetProvider contentSetProvider)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long
			testGetAssetLibraryContentSetProvidersPage_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	protected Long
			testGetAssetLibraryContentSetProvidersPage_getIrrelevantAssetLibraryId()
		throws Exception {

		return irrelevantDepotEntry.getDepotEntryId();
	}

	@Test
	public void testGetSiteContentSetProvidersPage() throws Exception {
		Long siteId = testGetSiteContentSetProvidersPage_getSiteId();
		Long irrelevantSiteId =
			testGetSiteContentSetProvidersPage_getIrrelevantSiteId();

		Page<ContentSetProvider> page =
			contentSetProviderResource.getSiteContentSetProvidersPage(
				siteId, RandomTestUtil.randomString(), null,
				Pagination.of(1, 10));

		long totalCount = page.getTotalCount();

		if (irrelevantSiteId != null) {
			ContentSetProvider irrelevantContentSetProvider =
				testGetSiteContentSetProvidersPage_addContentSetProvider(
					irrelevantSiteId, randomIrrelevantContentSetProvider());

			page = contentSetProviderResource.getSiteContentSetProvidersPage(
				irrelevantSiteId, null, null,
				Pagination.of(1, (int)totalCount + 1));

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantContentSetProvider,
				(List<ContentSetProvider>)page.getItems());
			assertValid(
				page,
				testGetSiteContentSetProvidersPage_getExpectedActions(
					irrelevantSiteId));
		}

		ContentSetProvider contentSetProvider1 =
			testGetSiteContentSetProvidersPage_addContentSetProvider(
				siteId, randomContentSetProvider());

		ContentSetProvider contentSetProvider2 =
			testGetSiteContentSetProvidersPage_addContentSetProvider(
				siteId, randomContentSetProvider());

		page = contentSetProviderResource.getSiteContentSetProvidersPage(
			siteId, null, null, Pagination.of(1, 10));

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			contentSetProvider1, (List<ContentSetProvider>)page.getItems());
		assertContains(
			contentSetProvider2, (List<ContentSetProvider>)page.getItems());
		assertValid(
			page,
			testGetSiteContentSetProvidersPage_getExpectedActions(siteId));
	}

	protected Map<String, Map<String, String>>
			testGetSiteContentSetProvidersPage_getExpectedActions(Long siteId)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	@Test
	public void testGetSiteContentSetProvidersPageWithPagination()
		throws Exception {

		Long siteId = testGetSiteContentSetProvidersPage_getSiteId();

		Page<ContentSetProvider> contentSetProvidersPage =
			contentSetProviderResource.getSiteContentSetProvidersPage(
				siteId, null, null, null);

		int totalCount = GetterUtil.getInteger(
			contentSetProvidersPage.getTotalCount());

		ContentSetProvider contentSetProvider1 =
			testGetSiteContentSetProvidersPage_addContentSetProvider(
				siteId, randomContentSetProvider());

		ContentSetProvider contentSetProvider2 =
			testGetSiteContentSetProvidersPage_addContentSetProvider(
				siteId, randomContentSetProvider());

		ContentSetProvider contentSetProvider3 =
			testGetSiteContentSetProvidersPage_addContentSetProvider(
				siteId, randomContentSetProvider());

		// See com.liferay.portal.vulcan.internal.configuration.HeadlessAPICompanyConfiguration#pageSizeLimit

		int pageSizeLimit = 500;

		if (totalCount >= (pageSizeLimit - 2)) {
			Page<ContentSetProvider> page1 =
				contentSetProviderResource.getSiteContentSetProvidersPage(
					siteId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 1.0) / pageSizeLimit),
						pageSizeLimit));

			Assert.assertEquals(totalCount + 3, page1.getTotalCount());

			assertContains(
				contentSetProvider1,
				(List<ContentSetProvider>)page1.getItems());

			Page<ContentSetProvider> page2 =
				contentSetProviderResource.getSiteContentSetProvidersPage(
					siteId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 2.0) / pageSizeLimit),
						pageSizeLimit));

			assertContains(
				contentSetProvider2,
				(List<ContentSetProvider>)page2.getItems());

			Page<ContentSetProvider> page3 =
				contentSetProviderResource.getSiteContentSetProvidersPage(
					siteId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 3.0) / pageSizeLimit),
						pageSizeLimit));

			assertContains(
				contentSetProvider3,
				(List<ContentSetProvider>)page3.getItems());
		}
		else {
			Page<ContentSetProvider> page1 =
				contentSetProviderResource.getSiteContentSetProvidersPage(
					siteId, null, null, Pagination.of(1, totalCount + 2));

			List<ContentSetProvider> contentSetProviders1 =
				(List<ContentSetProvider>)page1.getItems();

			Assert.assertEquals(
				contentSetProviders1.toString(), totalCount + 2,
				contentSetProviders1.size());

			Page<ContentSetProvider> page2 =
				contentSetProviderResource.getSiteContentSetProvidersPage(
					siteId, null, null, Pagination.of(2, totalCount + 2));

			Assert.assertEquals(totalCount + 3, page2.getTotalCount());

			List<ContentSetProvider> contentSetProviders2 =
				(List<ContentSetProvider>)page2.getItems();

			Assert.assertEquals(
				contentSetProviders2.toString(), 1,
				contentSetProviders2.size());

			Page<ContentSetProvider> page3 =
				contentSetProviderResource.getSiteContentSetProvidersPage(
					siteId, null, null, Pagination.of(1, (int)totalCount + 3));

			assertContains(
				contentSetProvider1,
				(List<ContentSetProvider>)page3.getItems());
			assertContains(
				contentSetProvider2,
				(List<ContentSetProvider>)page3.getItems());
			assertContains(
				contentSetProvider3,
				(List<ContentSetProvider>)page3.getItems());
		}
	}

	protected ContentSetProvider
			testGetSiteContentSetProvidersPage_addContentSetProvider(
				Long siteId, ContentSetProvider contentSetProvider)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetSiteContentSetProvidersPage_getSiteId()
		throws Exception {

		return testGroup.getGroupId();
	}

	protected Long testGetSiteContentSetProvidersPage_getIrrelevantSiteId()
		throws Exception {

		return irrelevantGroup.getGroupId();
	}

	@Test
	public void testBatchEngineDeleteImportTask() throws Exception {
		Assert.assertTrue(true);
	}

	protected void assertContains(
		ContentSetProvider contentSetProvider,
		List<ContentSetProvider> contentSetProviders) {

		boolean contains = false;

		for (ContentSetProvider item : contentSetProviders) {
			if (equals(contentSetProvider, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			contentSetProviders + " does not contain " + contentSetProvider,
			contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		ContentSetProvider contentSetProvider1,
		ContentSetProvider contentSetProvider2) {

		Assert.assertTrue(
			contentSetProvider1 + " does not equal " + contentSetProvider2,
			equals(contentSetProvider1, contentSetProvider2));
	}

	protected void assertEquals(
		List<ContentSetProvider> contentSetProviders1,
		List<ContentSetProvider> contentSetProviders2) {

		Assert.assertEquals(
			contentSetProviders1.size(), contentSetProviders2.size());

		for (int i = 0; i < contentSetProviders1.size(); i++) {
			ContentSetProvider contentSetProvider1 = contentSetProviders1.get(
				i);
			ContentSetProvider contentSetProvider2 = contentSetProviders2.get(
				i);

			assertEquals(contentSetProvider1, contentSetProvider2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<ContentSetProvider> contentSetProviders1,
		List<ContentSetProvider> contentSetProviders2) {

		Assert.assertEquals(
			contentSetProviders1.size(), contentSetProviders2.size());

		for (ContentSetProvider contentSetProvider1 : contentSetProviders1) {
			boolean contains = false;

			for (ContentSetProvider contentSetProvider2 :
					contentSetProviders2) {

				if (equals(contentSetProvider1, contentSetProvider2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				contentSetProviders2 + " does not contain " +
					contentSetProvider1,
				contains);
		}
	}

	protected void assertValid(ContentSetProvider contentSetProvider)
		throws Exception {

		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("itemSubtype", additionalAssertFieldName)) {
				if (contentSetProvider.getItemSubtype() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("itemType", additionalAssertFieldName)) {
				if (contentSetProvider.getItemType() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("key", additionalAssertFieldName)) {
				if (contentSetProvider.getKey() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("title", additionalAssertFieldName)) {
				if (contentSetProvider.getTitle() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<ContentSetProvider> page) {
		assertValid(page, Collections.emptyMap());
	}

	protected void assertValid(
		Page<ContentSetProvider> page,
		Map<String, Map<String, String>> expectedActions) {

		boolean valid = false;

		java.util.Collection<ContentSetProvider> contentSetProviders =
			page.getItems();

		int size = contentSetProviders.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);

		assertValid(page.getActions(), expectedActions);
	}

	protected void assertValid(
		Map<String, Map<String, String>> actions1,
		Map<String, Map<String, String>> actions2) {

		for (String key : actions2.keySet()) {
			Map action = actions1.get(key);

			Assert.assertNotNull(key + " does not contain an action", action);

			Map<String, String> expectedAction = actions2.get(key);

			Assert.assertEquals(
				expectedAction.get("method"), action.get("method"));
			Assert.assertEquals(expectedAction.get("href"), action.get("href"));
		}
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field :
				getDeclaredFields(
					com.liferay.headless.delivery.dto.v1_0.ContentSetProvider.
						class)) {

			if (!ArrayUtil.contains(
					getAdditionalAssertFieldNames(), field.getName())) {

				continue;
			}

			graphQLFields.addAll(getGraphQLFields(field));
		}

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(
		ContentSetProvider contentSetProvider1,
		ContentSetProvider contentSetProvider2) {

		if (contentSetProvider1 == contentSetProvider2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("itemSubtype", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						contentSetProvider1.getItemSubtype(),
						contentSetProvider2.getItemSubtype())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("itemType", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						contentSetProvider1.getItemType(),
						contentSetProvider2.getItemType())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("key", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						contentSetProvider1.getKey(),
						contentSetProvider2.getKey())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("title", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						contentSetProvider1.getTitle(),
						contentSetProvider2.getTitle())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		if (clazz.getClassLoader() == null) {
			return new java.lang.reflect.Field[0];
		}

		return TransformUtil.transform(
			ReflectionUtil.getDeclaredFields(clazz),
			field -> {
				if (field.isSynthetic()) {
					return null;
				}

				return field;
			},
			java.lang.reflect.Field.class);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_contentSetProviderResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_contentSetProviderResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		if (entityModel == null) {
			return Collections.emptyList();
		}

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		return TransformUtil.transform(
			getEntityFields(),
			entityField -> {
				if (!Objects.equals(entityField.getType(), type) ||
					ArrayUtil.contains(
						getIgnoredEntityFieldNames(), entityField.getName())) {

					return null;
				}

				return entityField;
			});
	}

	protected String getFilterString(
		EntityField entityField, String operator,
		ContentSetProvider contentSetProvider) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("itemSubtype")) {
			Object object = contentSetProvider.getItemSubtype();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("itemType")) {
			Object object = contentSetProvider.getItemType();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("key")) {
			Object object = contentSetProvider.getKey();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("title")) {
			Object object = contentSetProvider.getTitle();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path(
			"http://localhost:" + PortalUtil.getPortalServerPort(false) +
				"/o/graphql");
		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected ContentSetProvider randomContentSetProvider() throws Exception {
		return new ContentSetProvider() {
			{
				itemSubtype = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				itemType = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				key = StringUtil.toLowerCase(RandomTestUtil.randomString());
				title = StringUtil.toLowerCase(RandomTestUtil.randomString());
			}
		};
	}

	protected ContentSetProvider randomIrrelevantContentSetProvider()
		throws Exception {

		ContentSetProvider randomIrrelevantContentSetProvider =
			randomContentSetProvider();

		return randomIrrelevantContentSetProvider;
	}

	protected ContentSetProvider randomPatchContentSetProvider()
		throws Exception {

		return randomContentSetProvider();
	}

	protected ContentSetProviderResource contentSetProviderResource;
	protected com.liferay.portal.kernel.model.Group irrelevantGroup;
	protected com.liferay.portal.kernel.model.Company testCompany;
	protected DepotEntry irrelevantDepotEntry;
	protected com.liferay.portal.kernel.model.Group irrelevantDepotEntryGroup;
	protected DepotEntry testDepotEntry;
	protected com.liferay.portal.kernel.model.Group testDepotEntryGroup;
	protected com.liferay.portal.kernel.model.Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = source.getClass();

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					_getAllDeclaredFields(sourceClass)) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				try {
					Method setMethod = _getMethod(
						targetClass, field.getName(), "set",
						getMethod.getReturnType());

					setMethod.invoke(target, getMethod.invoke(source));
				}
				catch (Exception e) {
					continue;
				}
			}
		}

		public static boolean hasProperty(Object bean, String name) {
			Method setMethod = _getMethod(
				bean.getClass(), "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod != null) {
				return true;
			}

			return false;
		}

		public static void setProperty(Object bean, String name, Object value)
			throws Exception {

			Class<?> clazz = bean.getClass();

			Method setMethod = _getMethod(
				clazz, "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod == null) {
				throw new NoSuchMethodException();
			}

			Class<?>[] parameterTypes = setMethod.getParameterTypes();

			setMethod.invoke(bean, _translateValue(parameterTypes[0], value));
		}

		private static List<java.lang.reflect.Field> _getAllDeclaredFields(
			Class<?> clazz) {

			List<java.lang.reflect.Field> fields = new ArrayList<>();

			while ((clazz != null) && (clazz != Object.class)) {
				for (java.lang.reflect.Field field :
						clazz.getDeclaredFields()) {

					fields.add(field);
				}

				clazz = clazz.getSuperclass();
			}

			return fields;
		}

		private static Method _getMethod(Class<?> clazz, String name) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) &&
					(method.getParameterCount() == 1) &&
					_parameterTypes.contains(method.getParameterTypes()[0])) {

					return method;
				}
			}

			return null;
		}

		private static Method _getMethod(
				Class<?> clazz, String fieldName, String prefix,
				Class<?>... parameterTypes)
			throws Exception {

			return clazz.getMethod(
				prefix + StringUtil.upperCaseFirstLetter(fieldName),
				parameterTypes);
		}

		private static Object _translateValue(
			Class<?> parameterType, Object value) {

			if ((value instanceof Integer) &&
				parameterType.equals(Long.class)) {

				Integer intValue = (Integer)value;

				return intValue.longValue();
			}

			return value;
		}

		private static final Set<Class<?>> _parameterTypes = new HashSet<>(
			Arrays.asList(
				Boolean.class, Date.class, Double.class, Integer.class,
				Long.class, Map.class, String.class));

	}

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BaseContentSetProviderResourceTestCase.class);

	private static Format _format;

	private com.liferay.portal.kernel.model.User _testCompanyAdminUser;

	@Inject
	private
		com.liferay.headless.delivery.resource.v1_0.ContentSetProviderResource
			_contentSetProviderResource;

}
// LIFERAY-REST-BUILDER-HASH:232602198