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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.display.context;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSet;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSetIndexReader;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexName;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexNameBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Petteri Karttunen
 */
public class EditMisspellingSetDisplayBuilder {

	public EditMisspellingSetDisplayBuilder(
		HttpServletRequest httpServletRequest, Portal portal,
		RenderRequest renderRequest, RenderResponse renderResponse,
		MisspellingSetIndexNameBuilder misspellingSetIndexNameBuilder,
		MisspellingSetIndexReader misspellingSetIndexReader) {

		_httpServletRequest = httpServletRequest;
		_portal = portal;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_misspellingSetIndexNameBuilder = misspellingSetIndexNameBuilder;
		_misspellingSetIndexReader = misspellingSetIndexReader;
	}

	public EditMisspellingSetDisplayContext build() {
		EditMisspellingSetDisplayContext editMisspellingSetDisplayContext =
			new EditMisspellingSetDisplayContext();

		_misspellingSetOptional = _getMisspellingSetOptional(_getCompanyId());

		_setBackURL(editMisspellingSetDisplayContext);
		_setData(editMisspellingSetDisplayContext);
		_setFormName(editMisspellingSetDisplayContext);
		_setInputName(editMisspellingSetDisplayContext);
		_setRedirect(editMisspellingSetDisplayContext);
		_setMisspellingSetId(editMisspellingSetDisplayContext);

		return editMisspellingSetDisplayContext;
	}

	private String _getBackURL() {
		return ParamUtil.getString(
			_httpServletRequest, "backURL", _getRedirect());
	}

	private long _getCompanyId() {
		return _portal.getCompanyId(_renderRequest);
	}

	private String _getFormName() {
		return "misspellingSetForm";
	}

	private String _getInputName() {
		return "misspellingSet";
	}

	private String _getLanguageId() {
		return _misspellingSetOptional.map(
			MisspellingSet::getLanguageId
		).orElse(
			StringPool.BLANK
		);
	}

	private List<String> _getMisspellings() {
		return _misspellingSetOptional.map(
			MisspellingSet::getMisspellings
		).orElse(
			new ArrayList<String>()
		);
	}

	private Optional<MisspellingSet> _getMisspellingSetOptional(
		long companyId) {

		MisspellingSetIndexName misspellingSetIndexName =
			_misspellingSetIndexNameBuilder.getMisspellingSetIndexName(
				companyId);

		return Optional.ofNullable(
			ParamUtil.getString(_renderRequest, "misspellingSetId", null)
		).flatMap(
			id -> _misspellingSetIndexReader.fetchOptional(
				misspellingSetIndexName, id)
		);
	}

	private String _getPhrase() {
		return _misspellingSetOptional.map(
			MisspellingSet::getPhrase
		).orElse(
			StringPool.BLANK
		);
	}

	private String _getRedirect() {
		return ParamUtil.getString(_httpServletRequest, "redirect");
	}

	private void _setBackURL(
		EditMisspellingSetDisplayContext editMisspellingSetDisplayContext) {

		editMisspellingSetDisplayContext.setBackURL(_getBackURL());
	}

	private void _setData(
		EditMisspellingSetDisplayContext editMisspellingSetDisplayContext) {

		editMisspellingSetDisplayContext.setData(
			HashMapBuilder.<String, Object>put(
				"formName", _renderResponse.getNamespace() + _getFormName()
			).put(
				"inputName", _renderResponse.getNamespace() + _getInputName()
			).put(
				"languageId", _getLanguageId()
			).put(
				"misspellings", _getMisspellings()
			).put(
				"phrase", _getPhrase()
			).build());
	}

	private void _setFormName(
		EditMisspellingSetDisplayContext editMisspellingSetDisplayContext) {

		editMisspellingSetDisplayContext.setFormName(_getFormName());
	}

	private void _setInputName(
		EditMisspellingSetDisplayContext editMisspellingSetDisplayContext) {

		editMisspellingSetDisplayContext.setInputName(_getInputName());
	}

	private void _setMisspellingSetId(
		EditMisspellingSetDisplayContext editMisspellingSetDisplayContext) {

		_misspellingSetOptional.ifPresent(
			misspellingSet ->
				editMisspellingSetDisplayContext.setMisspellingSetId(
					misspellingSet.getMisspellingSetId()));
	}

	private void _setRedirect(
		EditMisspellingSetDisplayContext editMisspellingSetDisplayContext) {

		editMisspellingSetDisplayContext.setRedirect(_getRedirect());
	}

	private final HttpServletRequest _httpServletRequest;
	private final MisspellingSetIndexNameBuilder
		_misspellingSetIndexNameBuilder;
	private final MisspellingSetIndexReader _misspellingSetIndexReader;
	private Optional<MisspellingSet> _misspellingSetOptional;
	private final Portal _portal;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;

}