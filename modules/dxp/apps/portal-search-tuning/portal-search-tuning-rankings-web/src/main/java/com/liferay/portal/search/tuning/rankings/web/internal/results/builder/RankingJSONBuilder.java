/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.tuning.rankings.web.internal.results.builder;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.document.library.configuration.DLConfiguration;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.security.permission.ResourceActions;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.FastDateFormatConstants;
import com.liferay.portal.kernel.util.FastDateFormatFactory;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.document.Document;

import jakarta.portlet.ResourceRequest;

import java.io.Serializable;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author André de Oliveira
 * @author Bryan Engler
 */
public class RankingJSONBuilder {

	public RankingJSONBuilder(
		DLAppLocalService dlAppLocalService,
		FastDateFormatFactory fastDateFormatFactory,
		ResourceActions resourceActions, ResourceRequest resourceRequest) {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		_dlAppLocalService = dlAppLocalService;
		_fastDateFormatFactory = fastDateFormatFactory;
		_resourceActions = resourceActions;

		_dlConfiguration = ConfigurableUtil.createConfigurable(
			DLConfiguration.class, new HashMap<String, Object>());
		_locale = themeDisplay.getLocale();
		_themeDisplay = themeDisplay;
	}

	public JSONObject build() {
		return JSONUtil.put(
			"author", _getAuthor()
		).put(
			"clicks", _document.getString("clicks")
		).put(
			"date", _getDateString()
		).put(
			"deleted", _deleted
		).put(
			"description", _getDescription()
		).put(
			"hidden", _hidden
		).put(
			"icon", _getIcon()
		).put(
			"id", _document.getString(Field.UID)
		).put(
			"pinned", _pinned
		).put(
			"title", _getTitle()
		).put(
			"type", _getType()
		).put(
			"viewURL", _viewURL
		);
	}

	public RankingJSONBuilder deleted(boolean deleted) {
		_deleted = deleted;

		return this;
	}

	public RankingJSONBuilder document(Document document) {
		_document = document;

		return this;
	}

	public RankingJSONBuilder hidden(boolean hidden) {
		_hidden = hidden;

		return this;
	}

	public RankingJSONBuilder pinned(boolean pinned) {
		_pinned = pinned;

		return this;
	}

	public RankingJSONBuilder viewURL(String viewURL) {
		_viewURL = viewURL;

		return this;
	}

	private boolean _containsMimeType(String[] mimeTypes, String mimeType) {
		for (String curMimeType : mimeTypes) {
			int pos = curMimeType.indexOf("/");

			if (pos != -1) {
				if (mimeType.equals(curMimeType)) {
					return true;
				}
			}
			else {
				if (mimeType.startsWith(curMimeType)) {
					return true;
				}
			}
		}

		return false;
	}

	private String _formatDate(Date date) {
		if (date == null) {
			return StringPool.BLANK;
		}

		Format format = _fastDateFormatFactory.getDateTime(
			FastDateFormatConstants.MEDIUM, FastDateFormatConstants.SHORT,
			_locale, _themeDisplay.getTimeZone());

		return format.format(date);
	}

	private AssetRenderer<?> _getAssetRenderer() {
		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				_document.getString(Field.ENTRY_CLASS_NAME));

		if (assetRendererFactory == null) {
			return null;
		}

		try {
			return assetRendererFactory.getAssetRenderer(
				_document.getLong(Field.ENTRY_CLASS_PK));
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Error getting AssetRenderer for " +
						_document.getLong(Field.ENTRY_CLASS_PK),
					portalException);
			}
		}

		return null;
	}

	private String _getAuthor() {
		if (_isUser()) {
			return _document.getString("screenName");
		}

		return _document.getString(Field.USER_NAME);
	}

	private String _getCMSObjectEntryIcon(ObjectDefinition objectDefinition) {
		String externalReferenceCode =
			objectDefinition.getExternalReferenceCode();

		if (Objects.equals(externalReferenceCode, "L_CMS_BASIC_DOCUMENT")) {
			String mimeType = _getCMSObjectEntryMimeType(objectDefinition);

			return _getIconFileMimeType(mimeType);
		}
		else if (Objects.equals(
					externalReferenceCode, "L_CMS_BASIC_WEB_CONTENT")) {

			return "forms";
		}
		else if (Objects.equals(externalReferenceCode, "L_CMS_BLOG")) {
			return "blogs";
		}
		else if (Objects.equals(
					externalReferenceCode, "L_CMS_EXTERNAL_VIDEO")) {

			return "document-multimedia";
		}

		return "web-content";
	}

	private String _getCMSObjectEntryMimeType(
		ObjectDefinition objectDefinition) {

		List<ObjectField> objectFields = objectDefinition.getObjectFieldBag(
		).getIndexedObjectFields();

		if (objectFields == null) {
			return null;
		}

		AssetRenderer<?> assetRenderer = _getAssetRenderer();

		if (assetRenderer == null) {
			return null;
		}

		ObjectEntry objectEntry = (ObjectEntry)assetRenderer.getAssetObject();

		for (ObjectField objectField : objectFields) {
			if (Objects.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_ATTACHMENT)) {

				Map<String, Serializable> values = objectEntry.getValues();

				Serializable fileEntryIdSerializable = values.get(
					objectField.getName());

				if (fileEntryIdSerializable == null) {
					continue;
				}

				long fileEntryId = GetterUtil.getLong(fileEntryIdSerializable);

				if (fileEntryId <= 0) {
					continue;
				}

				try {
					DLFileEntry dlFileEntry =
						DLFileEntryLocalServiceUtil.getDLFileEntry(fileEntryId);

					if (dlFileEntry != null) {
						return dlFileEntry.getMimeType();
					}
				}
				catch (PortalException portalException) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Error getting MIME type for attachment " +
								fileEntryId,
							portalException);
					}
				}
			}
		}

		return null;
	}

	private Date _getCreateDate() {
		String dateStringFieldValue = _document.getString(Field.CREATE_DATE);

		if (Validator.isNull(dateStringFieldValue)) {
			return null;
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		try {
			return dateFormat.parse(dateStringFieldValue);
		}
		catch (Exception exception) {
			throw new IllegalArgumentException(
				"Unable to parse date string: " + dateStringFieldValue,
				exception);
		}
	}

	private String _getDateString() {
		return _formatDate(_getCreateDate());
	}

	private String _getDescription() {
		String description = _document.getString(
			Field.getLocalizedName(_locale, Field.CONTENT));

		if (Validator.isBlank(description)) {
			description = _document.getString(Field.CONTENT);
		}

		if (Validator.isBlank(description)) {
			description = _document.getString(
				Field.getLocalizedName(_locale, Field.DESCRIPTION));
		}

		if (Validator.isBlank(description)) {
			description = _document.getString(Field.DESCRIPTION);
		}

		ObjectDefinition objectDefinition = _getObjectDefinition();

		if (objectDefinition != null) {
			description = _getObjectEntryContent();
		}

		return StringUtil.shorten(description, 200);
	}

	private String _getIcon() {
		if (_isFileEntry()) {
			long entryClassPK = _document.getLong(Field.ENTRY_CLASS_PK);

			try {
				FileEntry fileEntry = _dlAppLocalService.getFileEntry(
					entryClassPK);

				return _getIconFileMimeType(fileEntry.getMimeType());
			}
			catch (PortalException portalException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to get file entry for " + entryClassPK,
						portalException);
				}

				return "document-default";
			}
		}

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				_document.getString(Field.ENTRY_CLASS_NAME));

		if (assetRendererFactory != null) {
			String iconCSSClass = assetRendererFactory.getIconCssClass();

			ObjectDefinition objectDefinition = _getObjectDefinition();

			if (Validator.isBlank(iconCSSClass) && objectDefinition.isCMS()) {
				return _getCMSObjectEntryIcon(objectDefinition);
			}

			return iconCSSClass;
		}

		return null;
	}

	private String _getIconFileMimeType(String mimeType) {
		if (_containsMimeType(_dlConfiguration.codeFileMimeTypes(), mimeType)) {
			return "document-code";
		}
		else if (_containsMimeType(
					_dlConfiguration.compressedFileMimeTypes(), mimeType)) {

			return "document-compressed";
		}
		else if (_containsMimeType(
					_dlConfiguration.multimediaFileMimeTypes(), mimeType)) {

			if (mimeType.startsWith("image")) {
				return "document-image";
			}

			return "document-multimedia";
		}
		else if (_containsMimeType(
					_dlConfiguration.presentationFileMimeTypes(), mimeType)) {

			return "document-presentation";
		}
		else if (_containsMimeType(
					_dlConfiguration.spreadSheetFileMimeTypes(), mimeType)) {

			return "document-table";
		}
		else if (_containsMimeType(
					_dlConfiguration.textFileMimeTypes(), mimeType)) {

			return "document-text";
		}
		else if (_containsMimeType(
					_dlConfiguration.vectorialFileMimeTypes(), mimeType)) {

			return "document-vector";
		}

		return "document-default";
	}

	private ObjectDefinition _getObjectDefinition() {
		String entryClassName = _document.getString(Field.ENTRY_CLASS_NAME);

		return ObjectDefinitionLocalServiceUtil.
			fetchObjectDefinitionByClassName(
				_themeDisplay.getCompanyId(), entryClassName);
	}

	private String _getObjectEntryContent() {
		StringBundler sb = new StringBundler();

		Map<String, com.liferay.portal.search.document.Field> fields =
			_document.getFields();

		for (Map.Entry<String, com.liferay.portal.search.document.Field> entry :
				fields.entrySet()) {

			String fieldName = entry.getKey();

			if (fieldName.startsWith("snippet_nestedFieldArray.value")) {
				Field field = (Field)entry.getValue();

				sb.append(
					StringUtil.merge(
						field.getValues(), StringPool.TRIPLE_PERIOD));

				sb.append(StringPool.TRIPLE_PERIOD);
			}
		}

		if (sb.index() > 0) {
			sb.setIndex(sb.index() - 1);
		}

		String content = sb.toString();

		if (Validator.isBlank(content)) {
			content = _document.getString("objectEntryContent");
		}

		return content;
	}

	private String _getTitle() {
		if (_isUser()) {
			return _document.getString("fullName");
		}

		String title = _document.getString(
			Field.getLocalizedName(_locale, Field.TITLE));

		if (Validator.isBlank(title)) {
			title = _document.getString(Field.TITLE);
		}

		if (Validator.isBlank(title)) {
			title = _document.getString(
				Field.getLocalizedName(_locale, Field.NAME));
		}

		if (Validator.isBlank(title)) {
			title = _document.getString(Field.NAME);
		}

		if (Validator.isBlank(title)) {
			AssetRenderer<?> assetRenderer = _getAssetRenderer();

			if (assetRenderer != null) {
				title = assetRenderer.getTitle(_locale);
			}
		}

		return title;
	}

	private String _getType() {
		String entryClassName = _document.getString(Field.ENTRY_CLASS_NAME);

		return _resourceActions.getModelResource(_locale, entryClassName);
	}

	private boolean _isFileEntry() {
		String entryClassName = _document.getString(Field.ENTRY_CLASS_NAME);

		return entryClassName.equals(DLFileEntryConstants.getClassName());
	}

	private boolean _isUser() {
		String entryClassName = _document.getString(Field.ENTRY_CLASS_NAME);

		return entryClassName.equals(User.class.getName());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RankingJSONBuilder.class);

	private boolean _deleted;
	private final DLAppLocalService _dlAppLocalService;
	private final DLConfiguration _dlConfiguration;
	private Document _document;
	private final FastDateFormatFactory _fastDateFormatFactory;
	private boolean _hidden;
	private final Locale _locale;
	private boolean _pinned;
	private final ResourceActions _resourceActions;
	private final ThemeDisplay _themeDisplay;
	private String _viewURL;

}