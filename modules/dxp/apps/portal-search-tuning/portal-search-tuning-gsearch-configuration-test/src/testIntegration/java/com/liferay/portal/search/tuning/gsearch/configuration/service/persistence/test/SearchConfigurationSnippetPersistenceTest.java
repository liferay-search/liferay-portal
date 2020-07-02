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

package com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.search.tuning.gsearch.configuration.exception.NoSuchConfigurationSnippetException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.SearchConfigurationSnippet;
import com.liferay.portal.search.tuning.gsearch.configuration.service.SearchConfigurationSnippetLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SearchConfigurationSnippetPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.service.persistence.SearchConfigurationSnippetUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @generated
 */
@RunWith(Arquillian.class)
public class SearchConfigurationSnippetPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.portal.search.tuning.gsearch.configuration.service"));

	@Before
	public void setUp() {
		_persistence = SearchConfigurationSnippetUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SearchConfigurationSnippet> iterator =
			_searchConfigurationSnippets.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfigurationSnippet searchConfigurationSnippet =
			_persistence.create(pk);

		Assert.assertNotNull(searchConfigurationSnippet);

		Assert.assertEquals(searchConfigurationSnippet.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SearchConfigurationSnippet newSearchConfigurationSnippet =
			addSearchConfigurationSnippet();

		_persistence.remove(newSearchConfigurationSnippet);

		SearchConfigurationSnippet existingSearchConfigurationSnippet =
			_persistence.fetchByPrimaryKey(
				newSearchConfigurationSnippet.getPrimaryKey());

		Assert.assertNull(existingSearchConfigurationSnippet);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSearchConfigurationSnippet();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfigurationSnippet newSearchConfigurationSnippet =
			_persistence.create(pk);

		newSearchConfigurationSnippet.setMvccVersion(RandomTestUtil.nextLong());

		newSearchConfigurationSnippet.setGroupId(RandomTestUtil.nextLong());

		newSearchConfigurationSnippet.setCompanyId(RandomTestUtil.nextLong());

		newSearchConfigurationSnippet.setUserId(RandomTestUtil.nextLong());

		newSearchConfigurationSnippet.setUserName(
			RandomTestUtil.randomString());

		newSearchConfigurationSnippet.setCreateDate(RandomTestUtil.nextDate());

		newSearchConfigurationSnippet.setModifiedDate(
			RandomTestUtil.nextDate());

		newSearchConfigurationSnippet.setTitle(RandomTestUtil.randomString());

		newSearchConfigurationSnippet.setDescription(
			RandomTestUtil.randomString());

		newSearchConfigurationSnippet.setType(RandomTestUtil.nextInt());

		newSearchConfigurationSnippet.setSnippet(RandomTestUtil.randomString());

		_searchConfigurationSnippets.add(
			_persistence.update(newSearchConfigurationSnippet));

		SearchConfigurationSnippet existingSearchConfigurationSnippet =
			_persistence.findByPrimaryKey(
				newSearchConfigurationSnippet.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfigurationSnippet.getMvccVersion(),
			newSearchConfigurationSnippet.getMvccVersion());
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getSnippetId(),
			newSearchConfigurationSnippet.getSnippetId());
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getGroupId(),
			newSearchConfigurationSnippet.getGroupId());
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getCompanyId(),
			newSearchConfigurationSnippet.getCompanyId());
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getUserId(),
			newSearchConfigurationSnippet.getUserId());
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getUserName(),
			newSearchConfigurationSnippet.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingSearchConfigurationSnippet.getCreateDate()),
			Time.getShortTimestamp(
				newSearchConfigurationSnippet.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingSearchConfigurationSnippet.getModifiedDate()),
			Time.getShortTimestamp(
				newSearchConfigurationSnippet.getModifiedDate()));
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getTitle(),
			newSearchConfigurationSnippet.getTitle());
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getDescription(),
			newSearchConfigurationSnippet.getDescription());
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getType(),
			newSearchConfigurationSnippet.getType());
		Assert.assertEquals(
			existingSearchConfigurationSnippet.getSnippet(),
			newSearchConfigurationSnippet.getSnippet());
	}

	@Test
	public void testCountByCompanyId() throws Exception {
		_persistence.countByCompanyId(RandomTestUtil.nextLong());

		_persistence.countByCompanyId(0L);
	}

	@Test
	public void testCountByC_T() throws Exception {
		_persistence.countByC_T(
			RandomTestUtil.nextLong(), RandomTestUtil.nextInt());

		_persistence.countByC_T(0L, 0);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SearchConfigurationSnippet newSearchConfigurationSnippet =
			addSearchConfigurationSnippet();

		SearchConfigurationSnippet existingSearchConfigurationSnippet =
			_persistence.findByPrimaryKey(
				newSearchConfigurationSnippet.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfigurationSnippet, newSearchConfigurationSnippet);
	}

	@Test(expected = NoSuchConfigurationSnippetException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SearchConfigurationSnippet>
		getOrderByComparator() {

		return OrderByComparatorFactoryUtil.create(
			"SearchConfigurationSnippet", "mvccVersion", true, "snippetId",
			true, "groupId", true, "companyId", true, "userId", true,
			"userName", true, "createDate", true, "modifiedDate", true, "title",
			true, "description", true, "type", true, "snippet", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SearchConfigurationSnippet newSearchConfigurationSnippet =
			addSearchConfigurationSnippet();

		SearchConfigurationSnippet existingSearchConfigurationSnippet =
			_persistence.fetchByPrimaryKey(
				newSearchConfigurationSnippet.getPrimaryKey());

		Assert.assertEquals(
			existingSearchConfigurationSnippet, newSearchConfigurationSnippet);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SearchConfigurationSnippet missingSearchConfigurationSnippet =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSearchConfigurationSnippet);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SearchConfigurationSnippet newSearchConfigurationSnippet1 =
			addSearchConfigurationSnippet();
		SearchConfigurationSnippet newSearchConfigurationSnippet2 =
			addSearchConfigurationSnippet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfigurationSnippet1.getPrimaryKey());
		primaryKeys.add(newSearchConfigurationSnippet2.getPrimaryKey());

		Map<Serializable, SearchConfigurationSnippet>
			searchConfigurationSnippets = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(2, searchConfigurationSnippets.size());
		Assert.assertEquals(
			newSearchConfigurationSnippet1,
			searchConfigurationSnippets.get(
				newSearchConfigurationSnippet1.getPrimaryKey()));
		Assert.assertEquals(
			newSearchConfigurationSnippet2,
			searchConfigurationSnippets.get(
				newSearchConfigurationSnippet2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SearchConfigurationSnippet>
			searchConfigurationSnippets = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(searchConfigurationSnippets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SearchConfigurationSnippet newSearchConfigurationSnippet =
			addSearchConfigurationSnippet();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfigurationSnippet.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SearchConfigurationSnippet>
			searchConfigurationSnippets = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, searchConfigurationSnippets.size());
		Assert.assertEquals(
			newSearchConfigurationSnippet,
			searchConfigurationSnippets.get(
				newSearchConfigurationSnippet.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SearchConfigurationSnippet>
			searchConfigurationSnippets = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(searchConfigurationSnippets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SearchConfigurationSnippet newSearchConfigurationSnippet =
			addSearchConfigurationSnippet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSearchConfigurationSnippet.getPrimaryKey());

		Map<Serializable, SearchConfigurationSnippet>
			searchConfigurationSnippets = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, searchConfigurationSnippets.size());
		Assert.assertEquals(
			newSearchConfigurationSnippet,
			searchConfigurationSnippets.get(
				newSearchConfigurationSnippet.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SearchConfigurationSnippetLocalServiceUtil.
				getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod
				<SearchConfigurationSnippet>() {

				@Override
				public void performAction(
					SearchConfigurationSnippet searchConfigurationSnippet) {

					Assert.assertNotNull(searchConfigurationSnippet);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SearchConfigurationSnippet newSearchConfigurationSnippet =
			addSearchConfigurationSnippet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfigurationSnippet.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"snippetId", newSearchConfigurationSnippet.getSnippetId()));

		List<SearchConfigurationSnippet> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		SearchConfigurationSnippet existingSearchConfigurationSnippet =
			result.get(0);

		Assert.assertEquals(
			existingSearchConfigurationSnippet, newSearchConfigurationSnippet);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfigurationSnippet.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("snippetId", RandomTestUtil.nextLong()));

		List<SearchConfigurationSnippet> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SearchConfigurationSnippet newSearchConfigurationSnippet =
			addSearchConfigurationSnippet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfigurationSnippet.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(ProjectionFactoryUtil.property("snippetId"));

		Object newSnippetId = newSearchConfigurationSnippet.getSnippetId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"snippetId", new Object[] {newSnippetId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSnippetId = result.get(0);

		Assert.assertEquals(existingSnippetId, newSnippetId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SearchConfigurationSnippet.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(ProjectionFactoryUtil.property("snippetId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"snippetId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SearchConfigurationSnippet addSearchConfigurationSnippet()
		throws Exception {

		long pk = RandomTestUtil.nextLong();

		SearchConfigurationSnippet searchConfigurationSnippet =
			_persistence.create(pk);

		searchConfigurationSnippet.setMvccVersion(RandomTestUtil.nextLong());

		searchConfigurationSnippet.setGroupId(RandomTestUtil.nextLong());

		searchConfigurationSnippet.setCompanyId(RandomTestUtil.nextLong());

		searchConfigurationSnippet.setUserId(RandomTestUtil.nextLong());

		searchConfigurationSnippet.setUserName(RandomTestUtil.randomString());

		searchConfigurationSnippet.setCreateDate(RandomTestUtil.nextDate());

		searchConfigurationSnippet.setModifiedDate(RandomTestUtil.nextDate());

		searchConfigurationSnippet.setTitle(RandomTestUtil.randomString());

		searchConfigurationSnippet.setDescription(
			RandomTestUtil.randomString());

		searchConfigurationSnippet.setType(RandomTestUtil.nextInt());

		searchConfigurationSnippet.setSnippet(RandomTestUtil.randomString());

		_searchConfigurationSnippets.add(
			_persistence.update(searchConfigurationSnippet));

		return searchConfigurationSnippet;
	}

	private List<SearchConfigurationSnippet> _searchConfigurationSnippets =
		new ArrayList<SearchConfigurationSnippet>();
	private SearchConfigurationSnippetPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}