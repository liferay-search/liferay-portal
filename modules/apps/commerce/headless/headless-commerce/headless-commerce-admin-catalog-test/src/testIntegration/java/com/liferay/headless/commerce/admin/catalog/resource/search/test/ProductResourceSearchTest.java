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

package com.liferay.headless.commerce.admin.catalog.resource.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Product;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.ProductResource;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.SearchRetryFixture;
import com.liferay.portal.search.test.util.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author André de Oliveira
 */
@RunWith(Arquillian.class)
public class ProductResourceSearchTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		Group group = GroupTestUtil.addGroup();

		setUpProductResource(productResource, group);

		_clientProductResource = buildClientProductResource();
		_commerceCatalog = createCatalog(getServiceContext(group.getGroupId()));
		_group = group;
	}

	@After
	public void tearDown() throws Exception {
		cpDefinitionLocalService.deleteCPDefinitions(
			_commerceCatalog.getCompanyId());
	}

	@Test
	public void testCPDefinitionRelevantDocuments() throws Exception {
		List<com.liferay.headless.commerce.admin.catalog.dto.v1_0.Product>
			products = Arrays.asList(
				addProduct(), addProduct(), addProduct());

		List<Long> ids = getSortedList(
			products.stream(), product -> product.getId());

		SearchRetryFixture.builder(
		).build(
		).assertSearch(
			() -> {
				SearchResponse searchResponse = searcher.search(
					searchRequestBuilderFactory.builder(
					).companyId(
						_group.getCompanyId()
					).groupIds(
						_commerceCatalog.getGroupId()
					).fields(
						"*"
					).modelIndexerClasses(
						CPDefinition.class
					).build());

				DocumentsAssert.assertValuesIgnoreRelevance(
					searchResponse.getRequestString(),
					searchResponse.getDocumentsStream(), Field.ENTRY_CLASS_PK,
					ids.stream());
			}
		);

		SearchRetryFixture.builder(
		).build(
		).assertSearch(
			() -> {
				SearchResponse searchResponse = searcher.search(
					searchRequestBuilderFactory.builder(
					).companyId(
						_group.getCompanyId()
					).fields(
						"*"
					).modelIndexerClasses(
						CPInstance.class
					).build());

				DocumentsAssert.assertValuesIgnoreRelevance(
					searchResponse.getRequestString(),
					searchResponse.getDocumentsStream(), "CPDefinitionId",
					ids.stream());
			}
		);
	}

	@Test
	public void testNoAfterimagePostDelete() throws Exception {
		for (int i = 1; i <= 3; i++) {
			List<com.liferay.headless.commerce.admin.catalog.dto.v1_0.Product>
				products = Arrays.asList(
					addProduct(), addProduct(), addProduct());

			List<Long> productIds = getSortedList(
				products.stream(), product -> product.getProductId());

			System.out.println("PLACE A BREAKPOINT HERE, AND LET IT GO");

			assertProductsInPage(productIds);

			System.out.println("PLACE A BREAKPOINT HERE, LET IT GO, THEN CHECK THE BREAKPOINT IN: com.liferay.portal.search.elasticsearch7.internal.ElasticsearchIndexWriter.deleteDocument(SearchContext, String)");

			deleteProduct(productIds.get(1));

			assertProductsInPage(
				Arrays.asList(productIds.get(0), productIds.get(2)));

			cpDefinitionLocalService.deleteCPDefinitions(
				_commerceCatalog.getCompanyId());
		}
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	protected static <X, Y> List<Y> getSortedList(
		Stream<X> stream, Function<X, Y> mapper) {

		return stream.map(
			mapper
		).sorted(
		).collect(
			Collectors.toList()
		);
	}

	protected com.liferay.headless.commerce.admin.catalog.dto.v1_0.Product
			addProduct()
		throws Exception {

		return productResource.postProduct(randomProduct());
	}

	protected void assertProductsInPage(List<Long> productIds) {
		SearchRetryFixture.builder(
		).build(
		).assertSearch(
			() -> {
				List<Product> products = getProducts(1, 3);

				Assert.assertEquals(
					products.toString(), productIds,
					getSortedList(products.stream(), Product::getProductId));
			}
		);
	}

	protected ProductResource buildClientProductResource() {
		return ProductResource.builder(
		).authentication(
			"test@liferay.com", "test"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	protected CommerceCatalog createCatalog(ServiceContext serviceContext)
		throws Exception {

		return _addCommerceCatalog(
			serviceContext,
			_fetchPrimaryCommerceCurrency(serviceContext.getCompanyId()));
	}

	protected void deleteProduct(Long id) throws Exception {
		productResource.deleteProduct(id);
	}

	protected AcceptLanguage getAcceptLanguage() {
		return new AcceptLanguage() {

			@Override
			public List<Locale> getLocales() {
				return null;
			}

			@Override
			public String getPreferredLanguageId() {
				return _LANGUAGE_ID;
			}

			@Override
			public Locale getPreferredLocale() {
				return LocaleUtil.fromLanguageId(_LANGUAGE_ID);
			}

		};
	}

	protected List<Product> getProducts(int pageNumber, int pageSize) {
		try {
			Page<Product> page = _clientProductResource.getProductsPage(
				null, null, Pagination.of(pageNumber, pageSize), null);

			return (List<Product>)page.getItems();
		}
		catch (RuntimeException runtimeException) {
			throw runtimeException;
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	protected ServiceContext getServiceContext(long groupId) throws Exception {
		User user = getTestUser();
		Group group = _groupLocalService.getGroup(groupId);

		Locale locale = LocaleUtil.getSiteDefault();

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setCompanyId(group.getCompanyId());
		serviceContext.setLanguageId(LanguageUtil.getLanguageId(locale));
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setTimeZone(user.getTimeZone());
		serviceContext.setUserId(user.getUserId());

		return serviceContext;
	}

	protected User getTestUser() throws Exception {
		return _userLocalService.getUser(TestPropsValues.getUserId());
	}

	protected com.liferay.headless.commerce.admin.catalog.dto.v1_0.Product
		randomProduct() {

		return new com.liferay.headless.commerce.admin.catalog.dto.v1_0.
			Product() {

			{
				active = true;
				catalogId = _commerceCatalog.getCommerceCatalogId();
				createDate = RandomTestUtil.nextDate();
				defaultSku = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				displayDate = RandomTestUtil.nextDate();
				expirationDate = RandomTestUtil.nextDate();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				modifiedDate = RandomTestUtil.nextDate();
				name = Collections.singletonMap(
					_LANGUAGE_ID, RandomTestUtil.randomString());
				neverExpire = RandomTestUtil.randomBoolean();
				productId = RandomTestUtil.randomLong();
				productStatus = RandomTestUtil.randomInt();
				productType = SimpleCPTypeConstants.NAME;
				productTypeI18n = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				skuFormatted = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				thumbnail = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
			}
		};
	}

	protected void setUpProductResource(
			com.liferay.headless.commerce.admin.catalog.resource.v1_0.
				ProductResource productResource,
			Group group)
		throws Exception {

		productResource.setContextAcceptLanguage(getAcceptLanguage());
		productResource.setContextCompany(
			CompanyLocalServiceUtil.getCompany(group.getCompanyId()));
		productResource.setContextUser(getTestUser());
	}

	@Inject
	protected CPDefinitionLocalService cpDefinitionLocalService;

	@Inject
	protected
		com.liferay.headless.commerce.admin.catalog.resource.v1_0.
			ProductResource productResource;

	@Inject
	protected Searcher searcher;

	@Inject
	protected SearchRequestBuilderFactory searchRequestBuilderFactory;

	private CommerceCatalog _addCommerceCatalog(
			ServiceContext serviceContext, CommerceCurrency commerceCurrency)
		throws Exception {

		Group group = serviceContext.getScopeGroup();

		return _commerceCatalogLocalService.addCommerceCatalog(
			group.getName(serviceContext.getLanguageId()),
			commerceCurrency.getCode(), serviceContext.getLanguageId(),
			StringPool.BLANK, serviceContext);
	}

	private CommerceCurrency _fetchPrimaryCommerceCurrency(long companyId) {
		return _commerceCurrencyLocalService.fetchPrimaryCommerceCurrency(
			companyId);
	}

	private static final String _LANGUAGE_ID = "en_US";

	private ProductResource _clientProductResource;

	@DeleteAfterTestRun
	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Inject
	private CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private UserLocalService _userLocalService;

}