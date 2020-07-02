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

package com.liferay.portal.search.tuning.gsearch.parameter;

import com.liferay.portal.search.tuning.gsearch.message.Message;

import java.util.List;
import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public interface SearchParameterData {

	public void addMessage(Message message);

	public void addParameter(Parameter parameter);
	
	public List<Message> getMessages();

	public Optional<Parameter> getByConfigurationVariableName(String name);

	public Optional<Parameter> getByName(String name);

	public Optional<Parameter> getByRole(String role);

	public List<Parameter> getParameters();

	public boolean hasErrors();

	public boolean hasParameters();
}