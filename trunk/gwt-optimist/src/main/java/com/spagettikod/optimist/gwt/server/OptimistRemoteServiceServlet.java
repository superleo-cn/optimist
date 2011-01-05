package com.spagettikod.optimist.gwt.server;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.spagettikod.optimist.ModifiedByAnotherUserException;
import com.spagettikod.optimist.RemovedByAnotherUserException;

@SuppressWarnings("serial")
public class OptimistRemoteServiceServlet extends RemoteServiceServlet {

	protected SqlSession perThreadSqlSession;

	@Override
	protected void onAfterResponseSerialized(String serializedResponse) {
		super.onAfterResponseSerialized(serializedResponse);
		if (perThreadSqlSession != null) {
			perThreadSqlSession.close();
		}
	}

	@Override
	protected void doUnexpectedFailure(Throwable e) {
		if (perThreadSqlSession != null) {
			perThreadSqlSession.rollback();
			perThreadSqlSession.close();
		}
		Throwable cause = e;
		while (cause != null) {
			if (cause instanceof ModifiedByAnotherUserException) {
				try {
					perThreadResponse.get().setStatus(
							HttpServletResponse.SC_CONFLICT);
					perThreadResponse
							.get()
							.getWriter()
							.print(ModifiedByAnotherUserException.class
									.getName());
					return;
				} catch (IOException e1) {
					super.doUnexpectedFailure(e);
				}
			} else if (cause instanceof RemovedByAnotherUserException) {
				try {
					perThreadResponse.get().setStatus(
							HttpServletResponse.SC_GONE);
					perThreadResponse
							.get()
							.getWriter()
							.print(RemovedByAnotherUserException.class
									.getName());
					return;
				} catch (IOException e1) {
					super.doUnexpectedFailure(e);
				}
			}
			cause = cause.getCause();
		}
		super.doUnexpectedFailure(e);
	}

	@Override
	protected void onAfterRequestDeserialized(RPCRequest rpcRequest) {
		super.onAfterRequestDeserialized(rpcRequest);
		ServletContext ctx = super.getServletContext();
		SqlSessionFactory factory = (SqlSessionFactory) ctx
				.getAttribute(Optimist.OPTIMIST_MYBATIS_SESSION_FACTORY_KEY);
		perThreadSqlSession = factory.openSession();
	}

}
