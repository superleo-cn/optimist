package com.spagettikod.optimist.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

public abstract class OptimistAsyncCallback<T> implements AsyncCallback<T> {

	public abstract void onModifiedByAnotherUser();

	public abstract void onRemovedByAnotherUser();

	public void onFailure(Throwable caught) {
		if (caught instanceof StatusCodeException) {
			StatusCodeException e = (StatusCodeException) caught;
			GWT.log(e.getMessage());
			if (e.getStatusCode() == 409
					&& e.getMessage()
							.equals("409 com.spagettikod.optimist.ModifiedByAnotherUserException")) {
				onModifiedByAnotherUser();
			} else if (e.getStatusCode() == 410
					&& e.getMessage()
							.equals("410 com.spagettikod.optimist.RemovedByAnotherUserException")) {
				onRemovedByAnotherUser();
			}
		}
	}
}
