package com.example.app.member.command;

import com.example.app.shared.request.GetMemberRequest;
import com.example.app.shared.response.embedded.MemberResponse;
import com.nantaaditya.framework.command.executor.Command;

public interface GetMemberCommand extends Command<GetMemberRequest, MemberResponse> {

}
