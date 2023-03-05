package com.example.app.member.command;

import com.example.app.shared.request.CreateMemberRequest;
import com.example.app.shared.response.CreateMemberResponse;
import com.nantaaditya.framework.command.executor.Command;

public interface CreateMemberCommand extends Command<CreateMemberRequest, CreateMemberResponse> {

}
