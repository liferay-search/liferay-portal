
package com.liferay.portal.search.tuning.gsearch.impl.params;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.gsearch.impl.util.GSearchUtil;

import com.liferay.portal.search.tuning.gsearch.api.constants.ParameterNames;
import com.liferay.portal.search.tuning.gsearch.api.exception.ParameterValidationException;
import com.liferay.portal.search.tuning.gsearch.api.params.ParameterBuilder;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

import java.util.Map;

import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * Start parameter builder.
 *
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, 
	service = ParameterBuilder.class
)
public class StartParameterBuilder implements ParameterBuilder {

	@Override
	public void addParameter(QueryContext queryContext) throws Exception {
		PortletRequest portletRequest =
			GSearchUtil.getPortletRequestFromContext(queryContext);
		
		int start = ParamUtil.getInteger(
			portletRequest, ParameterNames.START, 0);

		queryContext.setStart(start);
	}

	@Override
	public void addParameterHeadless(
			QueryContext queryContext, Map<String, Object> parameters)
		throws Exception {

		int start = GetterUtil.getInteger(
			parameters.get(ParameterNames.START), 0);

		queryContext.setStart(start);
	}

	@Override
	public boolean validate(QueryContext queryContext)
		throws ParameterValidationException {

		return true;
	}

	@Override
	public boolean validateHeadless(
			QueryContext queryContext, Map<String, Object> parameters)
		throws ParameterValidationException {

		return true;
	}

}