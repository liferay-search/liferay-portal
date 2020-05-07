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

package com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.test;

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
import com.liferay.portal.search.tuning.gsearch.configuration.model.exception.NoSuchSortFieldException;
import com.liferay.portal.search.tuning.gsearch.configuration.model.model.SortField;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.SortFieldLocalServiceUtil;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.SortFieldPersistence;
import com.liferay.portal.search.tuning.gsearch.configuration.model.service.persistence.SortFieldUtil;
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
public class SortFieldPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.portal.search.tuning.gsearch.configuration.model.service"));

	@Before
	public void setUp() {
		_persistence = SortFieldUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<SortField> iterator = _sortFields.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SortField sortField = _persistence.create(pk);

		Assert.assertNotNull(sortField);

		Assert.assertEquals(sortField.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		SortField newSortField = addSortField();

		_persistence.remove(newSortField);

		SortField existingSortField = _persistence.fetchByPrimaryKey(
			newSortField.getPrimaryKey());

		Assert.assertNull(existingSortField);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addSortField();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SortField newSortField = _persistence.create(pk);

		newSortField.setGroupId(RandomTestUtil.nextLong());

		newSortField.setCompanyId(RandomTestUtil.nextLong());

		newSortField.setUserId(RandomTestUtil.nextLong());

		newSortField.setUserName(RandomTestUtil.randomString());

		newSortField.setCreateDate(RandomTestUtil.nextDate());

		newSortField.setModifiedDate(RandomTestUtil.nextDate());

		newSortField.setConfigurationSetId(RandomTestUtil.nextLong());

		newSortField.setSortField(RandomTestUtil.randomString());

		_sortFields.add(_persistence.update(newSortField));

		SortField existingSortField = _persistence.findByPrimaryKey(
			newSortField.getPrimaryKey());

		Assert.assertEquals(
			existingSortField.getSortFieldId(), newSortField.getSortFieldId());
		Assert.assertEquals(
			existingSortField.getGroupId(), newSortField.getGroupId());
		Assert.assertEquals(
			existingSortField.getCompanyId(), newSortField.getCompanyId());
		Assert.assertEquals(
			existingSortField.getUserId(), newSortField.getUserId());
		Assert.assertEquals(
			existingSortField.getUserName(), newSortField.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingSortField.getCreateDate()),
			Time.getShortTimestamp(newSortField.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(existingSortField.getModifiedDate()),
			Time.getShortTimestamp(newSortField.getModifiedDate()));
		Assert.assertEquals(
			existingSortField.getConfigurationSetId(),
			newSortField.getConfigurationSetId());
		Assert.assertEquals(
			existingSortField.getSortField(), newSortField.getSortField());
	}

	@Test
	public void testCountByConfigurationSetId() throws Exception {
		_persistence.countByConfigurationSetId(RandomTestUtil.nextLong());

		_persistence.countByConfigurationSetId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		SortField newSortField = addSortField();

		SortField existingSortField = _persistence.findByPrimaryKey(
			newSortField.getPrimaryKey());

		Assert.assertEquals(existingSortField, newSortField);
	}

	@Test(expected = NoSuchSortFieldException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<SortField> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"SortField", "sortFieldId", true, "groupId", true, "companyId",
			true, "userId", true, "userName", true, "createDate", true,
			"modifiedDate", true, "configurationSetId", true, "sortField",
			true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		SortField newSortField = addSortField();

		SortField existingSortField = _persistence.fetchByPrimaryKey(
			newSortField.getPrimaryKey());

		Assert.assertEquals(existingSortField, newSortField);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SortField missingSortField = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingSortField);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		SortField newSortField1 = addSortField();
		SortField newSortField2 = addSortField();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSortField1.getPrimaryKey());
		primaryKeys.add(newSortField2.getPrimaryKey());

		Map<Serializable, SortField> sortFields =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, sortFields.size());
		Assert.assertEquals(
			newSortField1, sortFields.get(newSortField1.getPrimaryKey()));
		Assert.assertEquals(
			newSortField2, sortFields.get(newSortField2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, SortField> sortFields =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(sortFields.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		SortField newSortField = addSortField();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSortField.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, SortField> sortFields =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, sortFields.size());
		Assert.assertEquals(
			newSortField, sortFields.get(newSortField.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, SortField> sortFields =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(sortFields.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		SortField newSortField = addSortField();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newSortField.getPrimaryKey());

		Map<Serializable, SortField> sortFields =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, sortFields.size());
		Assert.assertEquals(
			newSortField, sortFields.get(newSortField.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			SortFieldLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<SortField>() {

				@Override
				public void performAction(SortField sortField) {
					Assert.assertNotNull(sortField);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		SortField newSortField = addSortField();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SortField.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"sortFieldId", newSortField.getSortFieldId()));

		List<SortField> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		SortField existingSortField = result.get(0);

		Assert.assertEquals(existingSortField, newSortField);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SortField.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"sortFieldId", RandomTestUtil.nextLong()));

		List<SortField> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		SortField newSortField = addSortField();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SortField.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("sortFieldId"));

		Object newSortFieldId = newSortField.getSortFieldId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"sortFieldId", new Object[] {newSortFieldId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingSortFieldId = result.get(0);

		Assert.assertEquals(existingSortFieldId, newSortFieldId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			SortField.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("sortFieldId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"sortFieldId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected SortField addSortField() throws Exception {
		long pk = RandomTestUtil.nextLong();

		SortField sortField = _persistence.create(pk);

		sortField.setGroupId(RandomTestUtil.nextLong());

		sortField.setCompanyId(RandomTestUtil.nextLong());

		sortField.setUserId(RandomTestUtil.nextLong());

		sortField.setUserName(RandomTestUtil.randomString());

		sortField.setCreateDate(RandomTestUtil.nextDate());

		sortField.setModifiedDate(RandomTestUtil.nextDate());

		sortField.setConfigurationSetId(RandomTestUtil.nextLong());

		sortField.setSortField(RandomTestUtil.randomString());

		_sortFields.add(_persistence.update(sortField));

		return sortField;
	}

	private List<SortField> _sortFields = new ArrayList<SortField>();
	private SortFieldPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}