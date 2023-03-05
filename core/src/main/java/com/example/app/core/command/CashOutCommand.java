package com.example.app.core.command;

import com.example.app.shared.request.CashOutRequest;
import com.example.app.shared.response.CashOutResponse;
import com.nantaaditya.framework.command.executor.Command;

public interface CashOutCommand extends Command<CashOutRequest, CashOutResponse> {

}
