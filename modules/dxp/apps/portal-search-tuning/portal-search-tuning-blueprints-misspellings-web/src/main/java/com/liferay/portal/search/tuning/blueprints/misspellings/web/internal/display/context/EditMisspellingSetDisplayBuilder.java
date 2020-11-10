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
		EditMisspellingSetDisplayContext editMisspellingSetsDisplayContext =
			new EditMisspellingSetDisplayContext();

		_misspellingSetOptional = _getMisspellingSetOptional(_getCompanyId());

		_setBackURL(editMisspellingSetsDisplayContext);
		_setData(editMisspellingSetsDisplayContext);
		_setFormName(editMisspellingSetsDisplayContext);
		_setInputName(editMisspellingSetsDisplayContext);
		_setRedirect(editMisspellingSetsDisplayContext);
		_setMisspellingSetId(editMisspellingSetsDisplayContext);

		return editMisspellingSetsDisplayContext;
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

	private Optional<MisspellingSet> _getMisspellingSetOptional(long companyId) {
		MisspellingSetIndexName misspellingSetIndexName =
			_misspellingSetIndexNameBuilder.getMisspellingSetIndexName(companyId);

		return Optional.ofNullable(
			ParamUtil.getString(_renderRequest, "misspellingSetId", null)
		).flatMap(
			id -> _misspellingSetIndexReader.fetchOptional(misspellingSetIndexName, id)
		);
	}

	private List<String> _getMisspellings() {
		return _misspellingSetOptional.map(
			MisspellingSet::getMisspellings
		).orElse(
			new ArrayList<String>()
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
		EditMisspellingSetDisplayContext editMisspellingSetsDisplayContext) {

		editMisspellingSetsDisplayContext.setBackURL(_getBackURL());
	}

	private void _setData(
		EditMisspellingSetDisplayContext editMisspellingSetsDisplayContext) {

		editMisspellingSetsDisplayContext.setData(
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
		EditMisspellingSetDisplayContext editMisspellingSetsDisplayContext) {

		editMisspellingSetsDisplayContext.setFormName(_getFormName());
	}

	private void _setInputName(
		EditMisspellingSetDisplayContext editMisspellingSetsDisplayContext) {

		editMisspellingSetsDisplayContext.setInputName(_getInputName());
	}

	private void _setRedirect(
		EditMisspellingSetDisplayContext editMisspellingSetsDisplayContext) {

		editMisspellingSetsDisplayContext.setRedirect(_getRedirect());
	}

	private void _setMisspellingSetId(
		EditMisspellingSetDisplayContext editMisspellingSetsDisplayContext) {

		_misspellingSetOptional.ifPresent(
			misspellingSet -> editMisspellingSetsDisplayContext.setMisspellingSetId(
				misspellingSet.getMisspellingSetId()));
	}

	private final HttpServletRequest _httpServletRequest;
	private final Portal _portal;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final MisspellingSetIndexNameBuilder _misspellingSetIndexNameBuilder;
	private final MisspellingSetIndexReader _misspellingSetIndexReader;
	private Optional<MisspellingSet> _misspellingSetOptional;

}