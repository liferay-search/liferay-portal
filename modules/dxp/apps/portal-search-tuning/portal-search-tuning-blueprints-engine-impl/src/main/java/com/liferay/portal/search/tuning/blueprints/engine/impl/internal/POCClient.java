package com.liferay.portal.search.tuning.blueprints.engine.impl.internal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.blueprints.engine.response.ResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.util.SearchClientHelper;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Here are POC utilities and a few examples of how to consume the framework.
 *
 * @author Petteri Karttunen
 */
@Component(service = {})
public class POCClient {

	public JSONObject test(
		HttpServletRequest httpServletRequest, long blueprintId) {

		ResponseAttributes resultAttributes = new ResponseAttributes();

		try {
			return _searchClientHelper.search(
				httpServletRequest, resultAttributes, blueprintId);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		catch (SearchRequestDataException e) {
			e.printStackTrace();
		}
		catch (PortalException e) {
			e.printStackTrace();
		}

		return JSONFactoryUtil.createJSONObject();
	}

	public JSONObject testWithSearchContext(
		SearchContext searchContext, long blueprintId) {

		ResponseAttributes resultAttributes = new ResponseAttributes();

		try {
			SearchRequestContext searchRequestContext =
				_searchClientHelper.getSearchRequestContext(
					searchContext, blueprintId);

			SearchRequestData searchRequestData =
				_searchClientHelper.getSearchRequestData(searchRequestContext);

			SearchSearchResponse searchResponse =
				_searchClientHelper.getSearchResponse(
					searchRequestContext, searchRequestData);

			return _searchClientHelper.getSearchResults(
				searchRequestContext, searchResponse, resultAttributes);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		catch (SearchRequestDataException e) {
			e.printStackTrace();
		}
		catch (PortalException e) {
			e.printStackTrace();
		}

		return JSONFactoryUtil.createJSONObject();
	}

	@Reference
	private SearchClientHelper _searchClientHelper;

}