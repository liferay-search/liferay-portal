package com.liferay.portal.search.tuning.gsearch.impl.internal.parameter;

import com.liferay.portal.search.tuning.gsearch.message.Message;
import com.liferay.portal.search.tuning.gsearch.message.Severity;
import com.liferay.portal.search.tuning.gsearch.parameter.Parameter;
import com.liferay.portal.search.tuning.gsearch.parameter.SearchParameterData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SearchParameterDataImpl implements SearchParameterData {

	@Override
	public void addMessage(Message message) {
		_messages.add(message);
	}

	@Override
	public void addParameter(Parameter parameter) {
		_parameters.add(parameter);
	}

	@Override
	public Optional<Parameter> getByConfigurationVariableName(String name) {
		return _parameters.stream(
		).filter(
			p -> name.equals(p.getConfigurationVariable())
		).findFirst();
	}

	@Override
	public Optional<Parameter> getByName(String name) {
		return _parameters.stream(
		).filter(
			p -> name.equals(p.getName())
		).findFirst();
	}

	@Override
	public Optional<Parameter> getByRole(String role) {
		return _parameters.stream(
		).filter(
			p -> role.equals(
				p.getRoleOptional(
				).orElse(
					""
				))
		).findFirst();
	}

	@Override
	public List<Message> getMessages() {
		return _messages;
	}

	@Override
	public List<Parameter> getParameters() {
		return _parameters;
	}

	@Override
	public boolean hasErrors() {
		return _messages.stream(
		).anyMatch(
			m -> m.getSeverity(
			).equals(
				Severity.ERROR
			)
		);
	}

	@Override
	public boolean hasParameters() {
		return !_parameters.isEmpty();
	}

	private final List<Message> _messages = new ArrayList<>();
	private final List<Parameter> _parameters = new ArrayList<>();

}