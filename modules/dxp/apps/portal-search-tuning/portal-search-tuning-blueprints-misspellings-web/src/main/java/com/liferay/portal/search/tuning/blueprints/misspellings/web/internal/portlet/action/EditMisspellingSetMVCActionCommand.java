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

import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.constants.MisspellingsPortletKeys;
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
		"mvc.command.name=editMisspellingSet"
	},
	service = MVCActionCommand.class
)
public class EditMisspellingSetMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		updateMisspellingSet(actionRequest);

		sendRedirect(actionRequest, actionResponse);
	}

	protected Optional<MisspellingSet> getMisspellingSetOptional(
		MisspellingSetIndexName misspellingSetIndexName, String id) {

		if (id == null) {
			return Optional.empty();
		}

		return _misspellingSetIndexReader.fetchOptional(
			misspellingSetIndexName, id);
	}

	protected void updateMisspellingSet(ActionRequest actionRequest) {
		MisspellingSetIndexName misspellingSetIndexName =
			_misspellingSetIndexNameBuilder.getMisspellingSetIndexName(
				_portal.getCompanyId(actionRequest));

		String id = ParamUtil.getString(actionRequest, "id");

		Optional<MisspellingSet> misspellingSetOptional =
			getMisspellingSetOptional(misspellingSetIndexName, id);

		MisspellingSet.MisspellingSetBuilder misspellingSetBuilder =
			_getMisspellingSetBuilder(
				actionRequest, misspellingSetOptional, misspellingSetIndexName);

		MisspellingSet misspellingSet = _buildMisspellingSet(
			actionRequest, misspellingSetBuilder);

		if (misspellingSetOptional.isPresent()) {
			_misspellingSetIndexWriter.update(
				misspellingSetIndexName, misspellingSet);
		}
		else {
			_misspellingSetIndexWriter.create(
				misspellingSetIndexName, misspellingSet);
		}
	}

	private MisspellingSet _buildMisspellingSet(
		ActionRequest actionRequest,
		MisspellingSet.MisspellingSetBuilder misspellingSetBuilder) {

		return misspellingSetBuilder.name(
			_getName(actionRequest)
		).languageId(
			_getLanguageId(actionRequest)
		).misspellings(
			_getMisspellings(actionRequest)
		).modified(
			new Date()
		).phrase(
			_getPhrase(actionRequest)
		).build();
	}

	private String _getLanguageId(ActionRequest actionRequest) {
		return ParamUtil.getString(actionRequest, "languageId");
	}

	private List<String> _getMisspellings(ActionRequest actionRequest) {
		String[] values = ParamUtil.getStringValues(
			actionRequest, "misspellings");

		List<String> misspellings = new ArrayList<>();

		for (String s : values) {
			misspellings.add(StringUtil.toLowerCase(s));
		}

		return misspellings;
	}

	private MisspellingSet.MisspellingSetBuilder _getMisspellingSetBuilder(
		ActionRequest actionRequest,
		Optional<MisspellingSet> misspellingSetOptional,
		MisspellingSetIndexName misspellingSetIndexName) {

		if (misspellingSetOptional.isPresent()) {
			return new MisspellingSet.MisspellingSetBuilder(
				misspellingSetOptional.get());
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		User user = themeDisplay.getUser();

		return new MisspellingSet.MisspellingSetBuilder().companyId(
			themeDisplay.getCompanyId()
		).created(
			new Date()
		).misspellingSetId(
			_misspellingSetIndexReader.getNextMisspellingSetId(
				misspellingSetIndexName)
		).userId(
			themeDisplay.getUserId()
		).userName(
			user.getFullName()
		);
	}

	private String _getName(ActionRequest actionRequest) {
		return ParamUtil.getString(actionRequest, "name");
	}

	private String _getPhrase(ActionRequest actionRequest) {
		return ParamUtil.getString(actionRequest, "phrase");
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