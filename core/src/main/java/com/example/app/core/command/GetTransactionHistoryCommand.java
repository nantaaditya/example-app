package com.example.app.core.command;

import com.example.app.shared.request.GetTransactionHistoryRequest;
import com.example.app.shared.response.TransactionsResponse;
import com.nantaaditya.framework.command.executor.Command;

public interface GetTransactionHistoryCommand extends Command<GetTransactionHistoryRequest, TransactionsResponse> {

}
