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
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.constants.MisspellingsPortletKeys;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSet;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSetIndexReader;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSetIndexWriter;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexName;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexNameBuilder;

import java.util.ArrayList;
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

		long companyId = portal.getCompanyId(actionRequest);

		MisspellingSetIndexName misspellingSetIndexName =
			_misspellingSetIndexNameBuilder.getMisspellingSetIndexName(companyId);

		updateMisspellingSetIndex(
			misspellingSetIndexName,
			ParamUtil.getString(actionRequest, "languageId"),
			ParamUtil.getString(actionRequest, "phrase"),
			_getMisspellings(actionRequest),
			getMisspellingSetOptional(misspellingSetIndexName, actionRequest));

		sendRedirect(actionRequest, actionResponse);
	}

	private List<String> _getMisspellings(ActionRequest actionRequest) {
		String[] values = ParamUtil.getStringValues(actionRequest, "misspellings");
		
		List<String> misspellings = new ArrayList<String>();
		for (String s : values) {
			misspellings.add(StringUtil.toLowerCase(s));
		}
		
		return misspellings;
	}

	protected Optional<MisspellingSet> getMisspellingSetOptional(
		MisspellingSetIndexName misspellingSetIndexName, ActionRequest actionRequest) {

		return Optional.ofNullable(
			ParamUtil.getString(actionRequest, "misspellingSetId", null)
		).flatMap(
			id -> _misspellingSetIndexReader.fetchOptional(misspellingSetIndexName, id)
		);
	}

	protected void updateMisspellingSetIndex(
		MisspellingSetIndexName misspellingSetIndexName,
		String languageId, String phrase,
		List<String> misspellings, 
		Optional<MisspellingSet> misspellingSetOptional) {

		MisspellingSet.MisspellingSetBuilder misspellingSetBuilder =
			new MisspellingSet.MisspellingSetBuilder();

		misspellingSetBuilder.languageId(phrase);

		misspellingSetBuilder.phrase(phrase);

		misspellingSetBuilder.misspellings(misspellings);

		misspellingSetOptional.ifPresent(
			misspellingSet -> misspellingSetBuilder.misspellingSetId(misspellingSet.getMisspellingSetId()));

		if (misspellingSetOptional.isPresent()) {
			_misspellingSetIndexWriter.update(
				misspellingSetIndexName, misspellingSetBuilder.build());
		}
		else {
			_misspellingSetIndexWriter.create(
				misspellingSetIndexName, misspellingSetBuilder.build());
		}
	}

	@Reference
	protected Portal portal;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private MisspellingSetIndexNameBuilder _misspellingSetIndexNameBuilder;

	@Reference
	private MisspellingSetIndexReader _misspellingSetIndexReader;

	@Reference
	private MisspellingSetIndexWriter _misspellingSetIndexWriter;

}