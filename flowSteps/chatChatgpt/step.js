step.chatChatgpt = function (inputs) {

	var inputsLogic = {
		model: inputs.model || "",
		messages: inputs.messages || {},
		max_tokens: inputs.max_tokens || 16,
		temperature: inputs.temperature || 1,
		top_p: inputs.top_p || 1,
		n: inputs.n || 1,
		user: inputs.user || ""
	};

	var options = {
		path: "/v1/chat/completions",
		body: inputsLogic
	}

	return endpoint._post(options);
};