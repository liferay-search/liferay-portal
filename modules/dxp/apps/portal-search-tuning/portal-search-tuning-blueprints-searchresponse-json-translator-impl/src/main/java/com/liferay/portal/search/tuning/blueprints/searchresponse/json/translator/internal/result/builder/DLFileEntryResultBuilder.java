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

package com.liferay.portal.search.tuning.blueprints.searchresponse.json.translator.internal.result.builder;

import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.searchresponse.json.translator.spi.result.ResultBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true,
	property = "model.class.name=com.liferay.document.library.kernel.model.DLFileEntry",
	service = ResultBuilder.class
)
public class DLFileEntryResultBuilder
	extends BaseResultBuilder implements ResultBuilder {

	@Override
	public Map<String, String> getMetadata(
			Document document, BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		return HashMapBuilder.put(
			"dimensions", _getDimensions(document, blueprintsAttributes)
		).put(
			"format", _translateMimetype(document.getString("mimeType"))
		).put(
			"size", _getSize(document)
		).build();
	}

	@Override
	public String getThumbnail(
			Document document, BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		StringBundler sb = new StringBundler(7);

		sb.append("/documents/");
		sb.append(document.getString(Field.SCOPE_GROUP_ID));
		sb.append("/");
		sb.append(document.getString(Field.FOLDER_ID));
		sb.append("/");
		sb.append(document.getString("path"));
		sb.append("?imageThumbnail=1");

		return sb.toString();
	}

	private String _getDimensions(
			Document document, BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		StringBundler sb = new StringBundler(4);

		sb.append(
			document.getString(
				_getTikaRawMetadataField(blueprintsAttributes, "WIDTH")));
		sb.append(" x ");
		sb.append(
			document.getString(
				_getTikaRawMetadataField(blueprintsAttributes, "LENGTH")));
		sb.append(" px");

		return sb.toString();
	}

	private String _getSize(Document document) {
		long size = document.getLong("size");

		StringBundler sb = new StringBundler(2);

		if (size >= _MBYTES) {
			sb.append(Math.round(size / (float)_MBYTES));
			sb.append(" MB");
		}
		else if (size >= _KBYTES) {
			sb.append(Math.round(size / (float)_KBYTES));
			sb.append(" KB");
		}
		else {
			sb.append(1);
			sb.append(" KB");
		}

		return sb.toString();
	}

	private String _getTikaRawMetadataField(
			BlueprintsAttributes blueprintsAttributes, String key)
		throws Exception {

		StringBundler sb = new StringBundler(4);

		sb.append("ddm__text__");
		sb.append(String.valueOf(_getTikaRawStructureId(blueprintsAttributes)));
		sb.append("__TIFF_IMAGE_");
		sb.append(key);

		return sb.toString();
	}

	private long _getTikaRawStructureId(
			BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		long companyId = blueprintsAttributes.getCompanyId();

		if (_tikaRawStructureIdMap.get(companyId) == null) {
			DynamicQuery structureQuery =
				_ddmStructureLocalService.dynamicQuery();

			structureQuery.add(
				RestrictionsFactoryUtil.eq("structureKey", "TIKARAWMETADATA"));
			structureQuery.add(
				RestrictionsFactoryUtil.eq("companyId", companyId));

			List<DDMStructure> structures =
				_ddmStructureLocalService.dynamicQuery(structureQuery);

			DDMStructure structure = structures.get(0);

			_tikaRawStructureIdMap.put(companyId, structure.getStructureId());
		}

		return _tikaRawStructureIdMap.get(companyId);
	}

	private String _translateMimetype(String mimeType) {
		if (_mimeTypes.containsKey(mimeType)) {
			return _mimeTypes.get(mimeType);
		}
		else if (mimeType.startsWith("application_")) {
			return mimeType.split("application_")[1];
		}
		else if (mimeType.startsWith("image_")) {
			return mimeType.split("image_")[1];
		}
		else if (mimeType.startsWith("text_")) {
			return mimeType.split("text_")[1];
		}
		else if (mimeType.startsWith("video_")) {
			return mimeType.split("video_")[1];
		}

		return mimeType;
	}

	private static final long _KBYTES = 1024;

	private static final long _MBYTES = 1024 * 1024;

	private static final Map<String, String> _mimeTypes = HashMapBuilder.put(
		"application_vnd.ms-excel", "xls"
	).put(
		"application_vnd.ms-powerpoint", "ppt"
	).put(
		"application_vnd.ms-word", "doc"
	).put(
		"application_vnd.oasis.opendocument.presentation", "odp"
	).put(
		"application_vnd.oasis.opendocument.spreadsheet", "ods"
	).put(
		"application_vnd.oasis.opendocument.text", "odt"
	).put(
		"application_vnd.openxmlformats-officedocument.presentationml." +
			"presentation",
		"pptx"
	).put(
		"application_vnd.openxmlformats-officedocument.spreadsheetml.sheet",
		"xlsx"
	).put(
		"application_vnd.openxmlformats-officedocument.wordprocessingml." +
			"document",
		"docx"
	).build();

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private DLAppService _dLAppService;

	private final Map<Long, Long> _tikaRawStructureIdMap = new HashMap<>();

}