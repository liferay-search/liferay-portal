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

package com.liferay.search.experiences.blueprints.condition.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.search.experiences.blueprints.model.Blueprint;
import com.liferay.segments.criteria.Criteria;
import com.liferay.segments.criteria.CriteriaSerializer;
import com.liferay.segments.criteria.contributor.SegmentsCriteriaContributor;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.test.util.SegmentsTestUtil;

import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Wade Cao
 */
@RunWith(Arquillian.class)
public class BoostContentsInCategoryForAUserSegmentTest
	extends BaseBoostContentsInCategoryTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testContainsCondition() throws Exception {
		user = UserTestUtil.addUser(group.getGroupId());

		_segmentsEntry = _addSegmentsEntry();

		AssetVocabulary assetVocabulary =
			AssetVocabularyLocalServiceUtil.addDefaultVocabulary(
				group.getGroupId());

		_assetCategory = AssetCategoryLocalServiceUtil.addCategory(
			user.getUserId(), group.getGroupId(), "Promoted",
			assetVocabulary.getVocabularyId(), serviceContext);

		addJournalArticle("Coca Cola", "cola cola");

		serviceContext.setAssetCategoryIds(
			new long[] {_assetCategory.getCategoryId()});

		addJournalArticle("Pepsi Cola", "");

		Blueprint blueprint = addCompanyBlueprint(
			Collections.singletonMap(
				LocaleUtil.US, getClass().getName() + "Blueprint"),
			Collections.singletonMap(LocaleUtil.US, ""),
			getConfigurationString((JSONObject[])null), "");

		assertSearch(blueprint, null, "[coca cola, pepsi cola]", "cola", null);
		assertSearch(
			blueprint,
			getConfigurationString(
				getQueryElementJSONObject(
					1000, _assetCategory.getCategoryId())),
			"[pepsi cola, coca cola]", "cola", getSelectedElementString());
	}

	@Override
	protected JSONObject getConditions() {
		return JSONUtil.put(
			"contains",
			JSONUtil.put(
				"parameter_name", "${user.active_segment_entry_ids}"
			).put(
				"value",
				createJSONArray().put(_segmentsEntry.getSegmentsEntryId())
			));
	}

	@Override
	protected JSONObject getDescription() {
		return JSONUtil.put(
			"en_US",
			"Boost contents in a category for users belonging to a user " +
				"segment");
	}

	@Override
	protected JSONObject getTitle() {
		return JSONUtil.put(
			"en_US", "Boost Contents in a Category for a User Segment");
	}

	@Override
	protected JSONObject getUIConfigurationValuesJSONObject() {
		return JSONUtil.put(
			"asset_category_id", _assetCategory.getCategoryId()
		).put(
			"boost", 1000
		).put(
			"user_segment_ids",
			createJSONArray().put(_segmentsEntry.getSegmentsEntryId())
		);
	}

	private SegmentsEntry _addSegmentsEntry() throws Exception {
		Criteria criteria = new Criteria();

		_userSegmentsCriteriaContributor.contribute(
			criteria, String.format("(firstName eq '%s')", user.getFirstName()),
			Criteria.Conjunction.AND);

		return SegmentsTestUtil.addSegmentsEntry(
			group.getGroupId(), CriteriaSerializer.serialize(criteria),
			User.class.getName());
	}

	private AssetCategory _assetCategory;
	private SegmentsEntry _segmentsEntry;

	@Inject(
		filter = "segments.criteria.contributor.key=user",
		type = SegmentsCriteriaContributor.class
	)
	private SegmentsCriteriaContributor _userSegmentsCriteriaContributor;

}