package com.liferay.portal.search.tuning.blueprints.admin.web.internal.portlet.action;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.search.tuning.blueprints.admin.web.internal.constants.BlueprintsAdminMVCCommandNames;
import com.liferay.portal.search.tuning.blueprints.constants.BlueprintsPortletKeys;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author Kevin Tan
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + BlueprintsPortletKeys.BLUEPRINTS_ADMIN,
		"mvc.command.name=" + BlueprintsAdminMVCCommandNames.SELECT_BLUEPRINT
	},
	service = MVCRenderCommand.class
)
public class SelectBlueprintMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		return "/select_blueprint.jsp";
	}

}