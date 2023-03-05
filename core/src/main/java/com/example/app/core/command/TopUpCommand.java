package com.example.app.core.command;

import com.example.app.shared.request.TopUpRequest;
import com.example.app.shared.response.TopUpResponse;
import com.nantaaditya.framework.command.executor.Command;

public interface TopUpCommand extends Command<TopUpRequest, TopUpResponse> {

}
