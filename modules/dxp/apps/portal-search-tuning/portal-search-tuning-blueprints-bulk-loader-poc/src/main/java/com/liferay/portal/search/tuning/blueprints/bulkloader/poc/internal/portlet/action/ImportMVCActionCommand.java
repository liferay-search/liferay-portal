package com.liferay.portal.search.tuning.blueprints.bulkloader.poc.internal.portlet.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PwdGenerator;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.tuning.blueprints.bulkloader.poc.internal.constants.BulkloaderPortletKeys;
import com.liferay.portal.search.tuning.blueprints.bulkloader.poc.internal.constants.MVCActionCommandNames;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
		"javax.portlet.name=" + BulkloaderPortletKeys.BULK_LOADER,
		"mvc.command.name=" + MVCActionCommandNames.IMPORT
	},
	service = MVCActionCommand.class
)
public class ImportMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		// List of userIds to be used as article creators.

		String importType = ParamUtil.getString(actionRequest, "type");

		String users = ParamUtil.getString(actionRequest, "userIds");

		List<Long> userIds = _createIdList(users);

		// List of groupIds to be used as target groups.

		String groups = ParamUtil.getString(actionRequest, "groupIds");

		List<Long> groupIds = _createIdList(groups);

		// Language id

		String languageId = ParamUtil.getString(
			actionRequest, "languageId", "en_US");

		// Disable link validation.

		ExportImportThreadLocal.setPortletImportInProcess(true);

		_importArticles(
			actionRequest, userIds, groupIds, languageId, importType);

		ExportImportThreadLocal.setPortletImportInProcess(false);
	}

	private void _addJournalArticle(
			ActionRequest actionRequest, long userId, long groupId,
			String languageId, String title, String content,
			String[] assetTagNames)
		throws Exception {

		long timer = System.currentTimeMillis();

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			JournalArticle.class.getName(), actionRequest);

		serviceContext.setAssetTagNames(assetTagNames);
		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);

		Locale locale = LocaleUtil.fromLanguageId(languageId);

		Map<Locale, String> titleMap = new HashMap<>();

		titleMap.put(locale, title);

		Map<Locale, String> descriptionMap = new HashMap<>();

		descriptionMap.put(locale, content);

		String instanceId = _generateInstanceId();

		String xmlContent =
			"<root available-locales=\"en_US\" default-locale=\"" + languageId +
				"\">" +
					"<dynamic-element name=\"content\" type=\"text_area\" index-type=\"text\" instance-id=\"" +
						instanceId + "\">" + "<dynamic-content language-id=\"" +
							languageId + "\"><![CDATA[" + content +
								"]]></dynamic-content>" + "</dynamic-element>" +
									"</root>";

		 _journalArticleLocalService.addArticle(
				 userId,
				 groupId,
				 0, // folderId,
				titleMap,
				descriptionMap,
				xmlContent,
				"BASIC-WEB-CONTENT", // ddmStructureKey,
				"BASIC-WEB-CONTENT", // ddmTemplateKey,
				serviceContext);

		_log.info(
			"Success. Took " + (System.currentTimeMillis() - timer) + " ms");
	}

	private boolean _checkTagValue(String value) {
		if (Validator.isBlank(value)) {
			return false;
		}

		char[] wordCharArray = value.toCharArray();

		for (char c : wordCharArray) {
			for (char invalidChar : _INVALID_CHARACTERS) {
				if (c == invalidChar) {
					return false;
				}
			}
		}

		return true;
	}

	private List<Long> _createIdList(String ids) {
		String[] arr = ids.split(",");
		List<Long> values = new ArrayList<>();

		for (String s : arr) {
			values.add(Long.valueOf(s));
		}

		return values;
	}

	private String _generateInstanceId() {
		StringBuilder instanceId = new StringBuilder(8);

		String key = PwdGenerator.KEY1 + PwdGenerator.KEY2 + PwdGenerator.KEY3;

		for (int i = 0; i < 8; i++) {
			int pos = (int)Math.floor(Math.random() * key.length());

			instanceId.append(key.charAt(pos));
		}

		return instanceId.toString();
	}

	private void _importArticles(
			ActionRequest actionRequest, List<Long> userIds,
			List<Long> groupIds, String languageId, String importType)
		throws Exception {

		int groupIdx = 0;
		int userIdx = 0;

		JsonParser parser = new JsonParser();

		Set<String> fileNames = fileNameCityMap.keySet();

		for (String fileName : fileNames) {
			if (importType.equals("restraunts") &&
				!fileName.endsWith("-restraunt.json")) {

				continue;
			}

			if (importType.equals("tourist attractions") &&
				!fileName.endsWith("-tourist.json")) {

				continue;
			}

			System.out.println("Importing " + fileName);

			InputStream in = this.getClass().getResourceAsStream(fileName);

			try {
				JsonElement root = parser.parse(
					new InputStreamReader(in));

				JsonObject rootobj = root.getAsJsonObject();

				JsonArray results = rootobj.getAsJsonArray("results");

				for (int i = 0; i < results.size(); i++) {
					JsonObject result = results.get(
						i
					).getAsJsonObject();

					// Content / City & geo

					JsonObject geometry = result.get(
						"geometry"
					).getAsJsonObject();

					JsonObject location = geometry.get(
						"location"
					).getAsJsonObject();

					String lat =  location.get(
						"lat"
					).getAsString();

					String lng =  location.get(
						"lng"
					).getAsString();

					String city = fileNameCityMap.get(fileName);

					String content = city + ":" + lat + "," + lng;

					// Title / Name

					String title = result.get(
						"name"
					).getAsString();

					// Categories => tags

					List<String> tags = new ArrayList<>();

					JsonArray types = result.getAsJsonArray("types");

					for (int j = 0; j < types.size(); j++) {
						String type = types.get(
							j
						).getAsString();

						if (_checkTagValue(type)) {

							// Replace underscores

							type = type.replace("_", " ");

							tags.add(type);
						}
					}

					String[] assetTagNames = tags.stream(
					).toArray(
						String[]::new
					);

					if (userIdx == userIds.size()) {
						userIdx = 0;
					}

					long userId = userIds.get(userIdx++);

					if (groupIdx == groupIds.size()) {
						groupIdx = 0;
					}

					long groupId = groupIds.get(groupIdx++);

					_log.info("Adding journal article " + title);

					_addJournalArticle(
						actionRequest, userId, groupId, languageId, title,
						content, assetTagNames);
				}
			}
			catch (Exception e) {
				_log.error(e.getMessage(), e);
			}
		}

		System.out.println("Fininshed importing all data");
	}

	private static final char[] _INVALID_CHARACTERS = {
		CharPool.AMPERSAND, CharPool.APOSTROPHE, CharPool.AT,
		CharPool.BACK_SLASH, CharPool.CLOSE_BRACKET, CharPool.CLOSE_CURLY_BRACE,
		CharPool.COLON, CharPool.COMMA, CharPool.EQUAL, CharPool.GREATER_THAN,
		CharPool.FORWARD_SLASH, CharPool.LESS_THAN, CharPool.NEW_LINE,
		CharPool.OPEN_BRACKET, CharPool.OPEN_CURLY_BRACE, CharPool.PERCENT,
		CharPool.PIPE, CharPool.PLUS, CharPool.POUND, CharPool.PRIME,
		CharPool.QUESTION, CharPool.QUOTE, CharPool.RETURN, CharPool.SEMICOLON,
		CharPool.SLASH, CharPool.STAR, CharPool.TILDE
	};

	private static final Log _log = LogFactoryUtil.getLog(
		ImportMVCActionCommand.class);

	/*
	Location data was found using the Google "Places API". Additional data sets
	can be added by copying the json results of a request into a file and
	putting an entry in fileNameCityMap.

	An API Key is needed to perform requests
	https://developers.google.com/places/web-service/get-api-key

	Example requests:
	Restraunts within ~10 miles of Los Angeles
	https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=$API_KEY&location=34.061645,-118.261353&radius=15000&type=restaurant

	Tourist Attractions within ~1 mile of New York
	https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=$API_KEY&location=40.761619,-73.972851&radius=1500&type=tourist_attraction

	List of supported "types"
	https://developers.google.com/places/web-service/supported_types

	json results formatted with https://jsonformatter.curiousconcept.com/
	 */

	private static final Map<String, String> fileNameCityMap =
		new HashMap<String, String>() {
			{
				put("la-restraunt.json","Los Angeles");
				put("la-tourist.json","Los Angeles");
				put("nashville-restraunt.json", "Nashville");
				put("nashville-tourist.json", "Nashville");
				put("ny-restraunt.json", "New York");
				put("ny-tourist.json", "New York");
			}
		};

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

}