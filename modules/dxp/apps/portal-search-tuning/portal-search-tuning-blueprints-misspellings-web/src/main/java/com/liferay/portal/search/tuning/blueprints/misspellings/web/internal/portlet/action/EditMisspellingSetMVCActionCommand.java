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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.portlet.action;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.constants.MisspellingsMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.constants.MisspellingsPortletKeys;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.constants.MisspellingsWebKeys;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSet;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSetIndexReader;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSetIndexWriter;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexName;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexNameBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + MisspellingsPortletKeys.MISSPELLINGS,
		"mvc.command.name=" + MisspellingsMVCCommandNames.EDIT_MISSPELLING_SET
	},
	service = MVCActionCommand.class
)
public class EditMisspellingSetMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		editMisspellingSet(actionRequest);

		sendRedirect(actionRequest, actionResponse);
	}

	protected void editMisspellingSet(ActionRequest actionRequest) {
		MisspellingSetIndexName misspellingSetIndexName =
			_misspellingSetIndexNameBuilder.getMisspellingSetIndexName(
				_portal.getCompanyId(actionRequest));

		Optional<MisspellingSet> misspellingSetOptional =
			_getMisspellingSetOptional(actionRequest, misspellingSetIndexName);

		if (misspellingSetOptional.isPresent()) {
			_updateMisspellingSet(
				actionRequest, misspellingSetIndexName,
				misspellingSetOptional.get());
		}
		else {
			_addMisspellingSet(actionRequest, misspellingSetIndexName);
		}
	}

	private void _addMisspellingSet(
		ActionRequest actionRequest,
		MisspellingSetIndexName misspellingSetIndexName) {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		_misspellingSetIndexWriter.create(
			misspellingSetIndexName,
			new MisspellingSet.MisspellingSetBuilder().companyId(
				themeDisplay.getCompanyId()
			).created(
				new Date()
			).groupId(
				_getGroupId(actionRequest)
			).languageId(
				_getLanguageId(actionRequest)
			).misspellings(
				_getMisspellings(actionRequest)
			).modified(
				new Date()
			).phrase(
				_getPhrase(actionRequest)
			).build());
	}

	private long _getGroupId(ActionRequest actionRequest) {
		return ParamUtil.getLong(actionRequest, MisspellingsWebKeys.GROUP_ID);
	}

	private String _getLanguageId(ActionRequest actionRequest) {
		return ParamUtil.getString(
			actionRequest, MisspellingsWebKeys.LANGUAGE_ID);
	}

	private List<String> _getMisspellings(ActionRequest actionRequest) {
		String[] values = ParamUtil.getStringValues(
			actionRequest, MisspellingsWebKeys.MISSPELLINGS);

		List<String> misspellings = new ArrayList<>();

		for (String s : values) {
			misspellings.add(StringUtil.toLowerCase(s));
		}

		return misspellings;
	}

	private Optional<MisspellingSet> _getMisspellingSetOptional(
		ActionRequest actionRequest,
		MisspellingSetIndexName misspellingSetIndexName) {

		return _misspellingSetIndexReader.fetchMisspellingSetOptional(
			misspellingSetIndexName,
			ParamUtil.getString(
				actionRequest, MisspellingsWebKeys.MISSPELLING_SET_ID));
	}

	private String _getPhrase(ActionRequest actionRequest) {
		return ParamUtil.getString(actionRequest, MisspellingsWebKeys.PHRASE);
	}

	private void _updateMisspellingSet(
		ActionRequest actionRequest,
		MisspellingSetIndexName misspellingSetIndexName,
		MisspellingSet misspellingSet) {

		_misspellingSetIndexWriter.update(
			misspellingSetIndexName,
			new MisspellingSet.MisspellingSetBuilder().groupId(
				_getGroupId(actionRequest)
			).languageId(
				_getLanguageId(actionRequest)
			).misspellings(
				_getMisspellings(actionRequest)
			).misspellingSetId(
				misspellingSet.getMisspellingSetId()
			).modified(
				new Date()
			).phrase(
				_getPhrase(actionRequest)
			).build());
	}

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private MisspellingSetIndexNameBuilder _misspellingSetIndexNameBuilder;

	@Reference
	private MisspellingSetIndexReader _misspellingSetIndexReader;

	@Reference
	private MisspellingSetIndexWriter _misspellingSetIndexWriter;

	@Reference
	private Portal _portal;

}